package com.kangmeng.message;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kangmeng.model.order.Cart;
import com.kangmeng.model.order.CartItem;
import com.kangmeng.model.order.SelectProductProperty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceMsgListener extends Thread {

    private ChannelHandlerContext ctx;

    private final static Logger logger = LoggerFactory.getLogger(DeviceMsgListener.class);

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private Properties properties;

    private String deviceId;

    private String topicName;

    private KafkaConsumer<String, String> consumer;

    private final Map<Integer, Long> partitionOffsets = new ConcurrentHashMap<>();

    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static final String DIVIDING_LINE;

    private static final int MAX_WIDTH = 32;

    private static final String LINE_BREAK = "*";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,##0.00");

    static {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < MAX_WIDTH; i++) {
            sb.append("-");
        }
        DIVIDING_LINE = sb.toString();
    }

    public DeviceMsgListener(ChannelHandlerContext ctx, Properties properties, String deviceId) {
        super(deviceId);

        this.ctx = ctx;
        this.properties = properties;
        this.deviceId = deviceId;
        this.topicName = "print-" + deviceId;
        createConsumer();
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    private void createConsumer() {
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, deviceId);
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topicName));
    }

    @Override
    public void run() {
        while (!closed.get()) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    Integer partition = Integer.parseInt("" + record.partition());
                    String topicName = record.topic();
                    Long offset = Long.parseLong("" + record.offset());
                    Cart cart = convertJson(record.value());
                    logger.info("topic is: {}, partition is: {},offset is {}, cart id is {}",
                            topicName, partition, offset, cart.getId());
                    consumerValue(cart, partition, offset);
                }
                commitOffsets();
            } catch (Exception ex) {
                logger.error("Error", ex);
            }
        }
        commitOffsets();
        consumer.close();
        shutdownLatch.countDown();
        logger.info("device : {}, consumer exited", deviceId);
    }

    public void shutdown() {
        try {
            closed.set(true);
            shutdownLatch.await();
        } catch (InterruptedException ex) {
            logger.error("Error", ex);
        }
    }

    public void addPrintedOffset(String offsetInfo) {
        String[] offsetInfoArr = offsetInfo.split("=");
        int partition = Integer.parseInt(offsetInfoArr[0]);
        long offset = Long.parseLong(offsetInfoArr[1]);
        if (!partitionOffsets.containsKey(partition)) {
            partitionOffsets.put(partition, offset);
        } else {
            long oldOffset = partitionOffsets.get(partition);
            if (partitionOffsets.get(partition) < offset) {
                partitionOffsets.put(partition, offset);
                logger.info("partition:{}, old offset:{},new offset: {}", partition, oldOffset, offset);
            }
        }
    }

    private void commitOffsets() {
        if (!partitionOffsets.isEmpty()) {
            Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
            for (Map.Entry<Integer, Long> entry : partitionOffsets.entrySet()) {
                partitionToMetadataMap.put(
                        new TopicPartition(topicName, entry.getKey()),
                        new OffsetAndMetadata(entry.getValue() + 1, "no metadata"));
            }
            consumer.commitSync(partitionToMetadataMap);
            partitionOffsets.clear();
            logger.info("topic: {}, committing the offsets : {}", topicName, partitionToMetadataMap);
        }
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

    private void consumerValue(Cart cart, int partition, long offset) {
        try {
            StringBuilder sb = new StringBuilder();
            // 数据起始标示符
            sb.append("&!");
            // 数据分隔符
            sb.append("*");
            sb.append("4");
            sb.append(partition).append("=").append(offset);
            // 接受方式
            sb.append("*");
            // 订单号
            sb.append("订单编号:").append(cart.getId());
            // 数据分隔符
            sb.append("*");
            sb.append(getPrintOrderString(cart));
            sb.append("*");
            if (cart.getMerchant().getPrintVoice() == null || cart.getMerchant().getPrintVoice()) {
                sb.append("<horn-50,1,1>");
            }

            // 数据结束标示符
            sb.append("#");

            //logger.info(sb.toString());

            byte[] bytes = sb.toString().getBytes("GBK");
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(bytes);
            ctx.writeAndFlush(byteBuf);
        } catch (Exception ex) {
            logger.info("handle cart error", ex);
        }
    }

    private String getPrintOrderString(Cart cart) {
        String val = getBlank(MAX_WIDTH, " ");

        if (cart.getNeedPay()) {
            String value = LINE_BREAK + "<S021>" + "订单已支付";
            if (cart.getTakeOut()) {
                value += getBlank(this.MAX_WIDTH - 14, " ") + "外卖";
            }
            val += value;
        } else if (cart.getTakeOut()) {
            val += LINE_BREAK + "<S021>" + getBlank(this.MAX_WIDTH - 4, " ") + "外卖";
        }
        if (cart.getTakeOut()) {
            val += LINE_BREAK + "用户名:" + cart.getName();
        } else {
            val += LINE_BREAK + "用户名:" + cart.getCustomer().getName();
        }

        if (cart.getTakeOut()) {
            val += LINE_BREAK + "电  话:" + cart.getPhone();
        } else {
            if (cart.getCustomer().getPhone() != null && !cart.getCustomer().getPhone().equals("")) {
                val += LINE_BREAK + "电  话:" + cart.getCustomer().getPhone();
            }
        }

        if (cart.getTakeOut()) {
            val += LINE_BREAK + "地  址:" + cart.getAddress();
        } else {
            if (cart.getCustomer().getAddress() != null && !cart.getCustomer().getAddress().equals("")) {
                val += LINE_BREAK + "地  址:" + cart.getCustomer().getAddress();
            }
        }

        val += LINE_BREAK + DIVIDING_LINE;
        val += LINE_BREAK + "下单时间:" + DATE_FORMAT.format(cart.getCreatedOn());
        val += LINE_BREAK + DIVIDING_LINE;
        val += LINE_BREAK + "商品名称" + getBlank(4, " ") + "数量" + getBlank(3, " ") + "价格" + getBlank(5, " ") + "金额";
        for (CartItem item : cart.getCartItems()) {
            val += LINE_BREAK + item.getName()+LINE_BREAK;
            val += getBlank(12, " ")+item.getQuantity()+ getBlank(7 - (""+item.getQuantity()).length(), " ");
            val += formatPrice(item.getUnitPrice().doubleValue());
            val += formatTotalPrice(item.getTotalPrice().doubleValue());

            for (SelectProductProperty selectProductProperty : item.getSelectProductProperties()) {
                val += LINE_BREAK + selectProductProperty.getName() + getBlank(1, " ") + selectProductProperty.getValue();
            }
        }

        if (cart.getRemark() != null) {
            val += LINE_BREAK + DIVIDING_LINE;
            val += LINE_BREAK + "备注: " + cart.getRemark();
        }

        val += LINE_BREAK + DIVIDING_LINE;

        if (cart.getTakeOutFee() != null && cart.getTakeOutFee().compareTo(BigDecimal.ZERO) != 0) {
            val += LINE_BREAK + formatTakeOutFee(cart.getTakeOutFee().doubleValue());
        }

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
        String priceStr = DECIMAL_FORMAT.format(price);
        return priceStr;
    }

    private String formatQuantity(int quantity) {
        return "" + quantity;
    }

    private String formatTotalPrice(double totalPrice) {
        String totalPriceStr = DECIMAL_FORMAT.format(totalPrice);
        return getBlank(7 - totalPriceStr.length(), " ") + totalPriceStr;
    }

    private String formatTakeOutFee(double takeOutFee) {
        String takeOutFeeStr = DECIMAL_FORMAT.format(takeOutFee);
        return getBlank(MAX_WIDTH - takeOutFeeStr.length() - 8, " ") + "快递费: " + takeOutFeeStr;
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
