package com.kangmeng.message;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kangmeng.model.order.Cart;
import com.kangmeng.model.order.CartItem;
import com.kangmeng.netty.ChannelCache;
import com.kangmeng.service.OffsetService;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
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

	private OffsetService offsetService;

	private KafkaConsumer<String, String> consumer;

	private String[] topics = new String[2];

	private static final String DIVIDING_LINE;

	private static final int MAX_WIDTH = 32;

	private static final String LINE_BREAK = "*";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,##0.00");

	static {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < MAX_WIDTH; i++) {
			sb.append("-");
		}
		DIVIDING_LINE = sb.toString();
	}

	public DeviceMsgListener(Properties properties, String deviceId, OffsetService offsetService) {
		super(deviceId);
		this.properties = properties;
		this.deviceId = deviceId;
		this.offsetService = offsetService;
		this.topics[0] = "print-"+deviceId;
		this.topics[1] = "manualprint-"+deviceId;
		createConsumer();
	}

	private void createConsumer() {
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, deviceId);
		consumer = new KafkaConsumer<>(properties);
		consumer.subscribe(Arrays.asList(topics));
	}

	@Override
	public void run() {
		try {
			consumer.poll(0);
			List<TopicPartition> partitions = new ArrayList<>();
			for (TopicPartition partition : consumer.assignment()) {
				if(partition.topic().startsWith("print-")){
					Long offset = getOffsetFromDB(partition);
					if(offset != null){
						consumer.seek(partition, offset+1);
					}else {
						partitions.add(partition);
					}
				}
			}
			if(partitions.size() >0){
				consumer.seekToEnd(partitions);
			}
			while (!closed.get()) {
				ConsumerRecords<String, String> records = consumer.poll(100);
				for (ConsumerRecord<String, String> record : records) {
					Integer partition = Integer.parseInt("" + record.partition());
					String topicName = record.topic();
					Long offset = Long.parseLong("" + record.offset());
					Cart cart = convertJson(record.value());
					logger.info("get from kfaka,topic is: {}, offset is {}, cart id is {}",topicName,offset,cart.getId());
					if(topicName.startsWith("print-")){
						saveOffset(cart.getId(), partition,offset);
					}
					consumerValue(cart);
				}
			}
		} catch (WakeupException e) {
			if (!closed.get())
				throw e;
		} finally {
			consumer.close();
		}
	}

	public void shutdown() {
		closed.set(true);
		consumer.wakeup();
	}

	private Cart convertJson(String cartJson) {
		Cart cart = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			cart = objectMapper.readValue(cartJson, Cart.class);
		} catch (Exception ex) {
			logger.info("parse json error", ex);
		}
		return cart;

	}

	private void saveOffset(Long cartId, Integer partition, Long offset) {
		this.offsetService.saveOffset(cartId, topics[0], partition, offset);
	}

	private Long getOffsetFromDB(TopicPartition partition) {
		Long offset = offsetService.getOffset(topics[0],partition.partition());
		return offset;
	}

	private void consumerValue(Cart cart) {
		try {
			StringBuilder sb = new StringBuilder();
			// 数据起始标示符
			sb.append("&!");
			// 数据分隔符
			sb.append("*");
			// 接受方式
			sb.append("1");
			// 订单号
			sb.append("订单编号:").append(cart.getId());
			// 数据分隔符
			sb.append("*");
			sb.append(getPrintOrderString(cart));
			sb.append("*");
			sb.append("<horn-50,1,1>");
			// 数据结束标示符
			sb.append("#");

			if (ChannelCache.INSTANCE.getChannel(deviceId) != null) {
				byte[] bytes = sb.toString().getBytes("GBK");
				ByteBuf byteBuf = Unpooled.buffer();
				byteBuf.writeBytes(bytes);
				ChannelCache.INSTANCE.getChannel(deviceId).writeAndFlush(byteBuf);
			}
		} catch (Exception ex) {
			logger.info("handle cart error", ex);
		}

	}

	private String getPrintOrderString(Cart cart) {
		String val = getBlank(MAX_WIDTH, " ");

		if(cart.getNeedPay()){
			String value = LINE_BREAK +"<S021>" + "订单已支付";
			if(cart.getTakeOut()){
				value+=getBlank(this.MAX_WIDTH - 14, " ")+"外卖";
			}
			val += value;
		}else if(cart.getTakeOut()){
			val += LINE_BREAK+"<S021>" +getBlank(this.MAX_WIDTH - 4, " ")+"外卖";
		}
		if(cart.getTakeOut()){
			val += LINE_BREAK + "用户名:" + cart.getName();
		}else{
			val += LINE_BREAK + "用户名:" + cart.getCustomer().getName();
		}

		if(cart.getTakeOut()){
			val += LINE_BREAK + "电  话:" + cart.getPhone();
		}else {
			if (cart.getCustomer().getPhone() != null && !cart.getCustomer().getPhone().equals("")) {
				val += LINE_BREAK + "电  话:" + cart.getCustomer().getPhone();
			}
		}

		if(cart.getTakeOut()){
			val += LINE_BREAK + "地  址:" + cart.getAddress();
		}else {
			if (cart.getCustomer().getAddress() != null && !cart.getCustomer().getAddress().equals("")) {
				val += LINE_BREAK + "地  址:" + cart.getCustomer().getAddress();
			}
		}

		val += LINE_BREAK + DIVIDING_LINE;
		val += LINE_BREAK + "下单时间:" + DATE_FORMAT.format(cart.getCreatedOn());
		val += LINE_BREAK + DIVIDING_LINE;
		val += LINE_BREAK + "商品名称" + getBlank(4, " ") + "价格" + getBlank(3, " ") + "数量" + getBlank(5, " ") + "金额";
		for (CartItem item : cart.getCartItems()) {
			String productName = item.getName();
			String name = productName.substring(0, productName.length() > 12 ? 12 : productName.length());
			val += LINE_BREAK + name;
			val += getBlank(12 - getLengthWithChinese(name), " ");

			String price = formatPrice(item.getUnitPrice().doubleValue());
			String quantity = formatQuantity(item.getQuantity());
			String productTotalPrice = formatTotalPrice(item.getTotalPrice().doubleValue());
			val += formatProduct(price + quantity + productTotalPrice);
		}

		if (cart.getRemark() != null) {
			val += LINE_BREAK + DIVIDING_LINE;
			val += LINE_BREAK + "备注: " + cart.getRemark();

		}

		val += LINE_BREAK + DIVIDING_LINE;

		val += LINE_BREAK + formatTotalSumPrice(cart.getTotalPrice().doubleValue());
		val += LINE_BREAK + getBlank(MAX_WIDTH, " ");
		val += LINE_BREAK + getCenterString("谢谢惠顾! 康萌预约宝", MAX_WIDTH);

		return val;
	}

	private String getBlank(int width, String remark) {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < width; i++) {
			sb.append(remark);
		}
		return sb.toString();
	}

	private String formatProduct(String s) {
		return s;
	}

	private String formatPrice(double price) {
		return DECIMAL_FORMAT.format(price);
	}

	private String formatQuantity(int quantity) {
		return getBlank(5 - ("" + quantity).length(), " ") + quantity;
	}

	private String formatTotalPrice(double totalPrice) {
		String totalPriceStr = DECIMAL_FORMAT.format(totalPrice);
		return getBlank(11 - totalPriceStr.length(), " ") + totalPriceStr;
	}

	private String formatTotalSumPrice(double totalSumPrice) {
		String totalSumPriceStr = DECIMAL_FORMAT.format(totalSumPrice);
		return getBlank(MAX_WIDTH - totalSumPriceStr.length() - 8, " ") + "总金额: " + totalSumPriceStr;
	}

	private String getCenterString(String val, int maxWidth) {
		String ret = "";
		if (val == null || val.length() == 0)
			return ret;

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
	 * @param validateStr
	 *            指定的字符串
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
	 * @param c
	 *            要判断的字符
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
