package com.kangmeng.message;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kangmeng.model.order.Cart;
import com.kangmeng.model.order.CartItem;
import com.kangmeng.netty.ChannelCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceMsgListener extends Thread {

	private final static Logger logger = LoggerFactory.getLogger(DeviceMsgListener.class);
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private Properties properties;
	private String deviceId;
	private KafkaConsumer<String, String> consumer;

	private static final String DIVIDING_LINE;

	private static final int LINE_SPACE = 40;

	private static final int MAX_WIDTH = 24;

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,##0.00");

	static {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < MAX_WIDTH; i++) {
			sb.append("-");
		}
		DIVIDING_LINE = sb.toString();
	}

	public DeviceMsgListener(Properties properties, String deviceId) {
		super(deviceId);
		this.properties = properties;
		this.deviceId = deviceId;
		createConsumer();
	}

	private void createConsumer() {
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, deviceId);
		consumer = new KafkaConsumer<>(properties);
		consumer.subscribe(Arrays.asList(deviceId));
	}

	@Override
	public void run() {
		try {
			while (!closed.get()) {
				ConsumerRecords<String, String> records = consumer.poll(100);
				for (ConsumerRecord<String, String> record : records){
					consumerValue(record.value());
					consumer.commitAsync();
				}
			}
		} catch (WakeupException e) {
			if (!closed.get())
				throw e;
		} finally {
			try {
				consumer.commitSync();
			} finally {
				consumer.close();
			}
		}
	}

	public void shutdown() {
		closed.set(true);
		consumer.wakeup();
	}

	public void consumerValue(String cartJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			Cart cart = objectMapper.readValue(cartJson, Cart.class);

			StringBuilder sb = new StringBuilder();
			// 数据起始标示符
			sb.append("&!");
			// 数据分隔符
			sb.append("*");
			// 接受方式
			sb.append("1");
			// 订单号
			sb.append(cart.getId());
			// 数据分隔符
			sb.append("*");
			sb.append("<S021>").append(getPrintOrderString(cart));
			// 数据结束标示符
			sb.append("#");

			if (ChannelCache.INSTANCE.getChannel(deviceId) != null) {
				byte[] bytes = sb.toString().getBytes("UTF-8");
				ByteBuf byteBuf = Unpooled.buffer();
				byteBuf.writeBytes(bytes);
				ChannelCache.INSTANCE.getChannel(deviceId).writeAndFlush(byteBuf);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private String getPrintOrderString(Cart cart) {
		String val = getBlank(MAX_WIDTH);

		String oneLineStr = "订单编号:" + cart.getId() + "  " + "用户名:" + cart.getCustomer().getName();
		int lengthWithChinese = getLengthWithChinese(oneLineStr);
		if (lengthWithChinese <= MAX_WIDTH) {
			// 一行显示
			val += "*" + oneLineStr;
		} else {
			// 两行显示
			val += "*" + "订单编号:" + cart.getId();
			val += "*" + "用户名:" + cart.getCustomer().getName();
		}

		val += "*" + "下单时间:" + DATE_FORMAT.format(cart.getCreatedOn());
		val += "*" + "提货时间:" + DATE_FORMAT.format(cart.getTakeTime());
		val += "*" + DIVIDING_LINE;

		val += "*" + "商品名称" + getBlank(4) + "价格" + getBlank(1) + "数量" + getBlank(7) + "金额";
		for(CartItem item : cart.getCartItems()){
			String productName = item.getName();
			String name = productName.substring(0,  productName.length() > 13 ? 13 : productName.length());
			val += "*" + name + "*";

			String price = formatPrice(item.getUnitPrice().doubleValue());
			String quantity = formatQuantity(item.getQuantity());
			String productTotalPrice = formatTotalPrice(item.getTotalPrice().doubleValue());
			val += formatProduct(price + quantity + productTotalPrice);
		}

		if(cart.getRemark() != null){
			val += "*" + DIVIDING_LINE;
			val += "*" + "备注: " + cart.getRemark();

		}

		val += "*" + DIVIDING_LINE;

		val += "*" + formatTotalSumPrice(cart.getTotalPrice().doubleValue());
		val += "*" + getBlank(MAX_WIDTH);
		val += "*" + getCenterString("谢谢惠顾! 康萌预约宝", MAX_WIDTH);

		return val;
	}

	private String getBlank(int width) {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < width; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	private String formatProduct(String s) {
		return getBlank(MAX_WIDTH - s.length()) + s;
	}

	private String formatPrice(double price) {
		return DECIMAL_FORMAT.format(price);
	}

	private String formatQuantity(int quantity) {
		return getBlank(5 - ("" + quantity).length()) + quantity;
	}

	private String formatTotalPrice(double totalPrice) {
		String totalPriceStr = DECIMAL_FORMAT.format(totalPrice);
		return getBlank(11 - totalPriceStr.length()) + totalPriceStr;
	}

	private String formatTotalSumPrice(double totalSumPrice) {
		String totalSumPriceStr = DECIMAL_FORMAT.format(totalSumPrice);
		return getBlank(MAX_WIDTH - totalSumPriceStr.length() - 8) + "总金额: " + totalSumPriceStr;
	}

	private String getCenterString(String val, int maxWidth) {
		String ret = "";
		if (val == null || val.length()==0) return ret;

		int chineseLength = getLengthWithChinese(val);
		if (maxWidth > chineseLength) {
			int d = (maxWidth - chineseLength) / 2;
			ret = String.format("%" + d + "s", " ");
			ret += val;
		} else {
			ret = val;
		}

		return ret;
	}

	/**
	 * 获取字符串的长度，如果有中文，则每个中文字符计为2位
	 *
	 * @param validateStr 指定的字符串
	 * @return 字符串的长度
	 */
	public int getLengthWithChinese(String validateStr) {
		if (validateStr == null || validateStr.length() == 0) {
			return 0;
		}

		int length = validateStr.length();

		char[] cs = validateStr.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			if (isChinese(cs[i])) {
				// 中文则再加1
				length++;
			}
		}

		return length;
	}

	/**
	 * 根据Unicode编码完美的判断中文汉字和符号
	 * http://www.micmiu.com/lang/java/java-check-chinese/
	 *
	 * @param c 要判断的字符
	 * @return 是否是中文字符
	 */
	public boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
	}
}
