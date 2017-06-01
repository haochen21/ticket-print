package com.kangmeng.order;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kangmeng.model.order.Cart;
import com.kangmeng.model.order.CartItem;
import com.kangmeng.netty.ChannelCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class OrderTest {

    int count = 1000;

    private static final String DIVIDING_LINE;

    private static final int LINE_SPACE = 40;

    private static final int MAX_WIDTH = 32;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,##0.00");

    private String cartJson = "{\"id\":3308137,\"no\":\"592d78fdb4c3eeb0136faef3\",\"transactionId\":null,\"merchant\":{\"id\":1896,\"loginName\":\"陈昊\",\"openId\":\"oyA2pjnuf-oFDaZi0BwUlZ_XsX2U\",\"name\":\"陈昊\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"deviceNo\":\"36074020000430\",\"phone\":\"13817475681\",\"mail\":\"chenhao21@163.com\",\"city\":\"Minhang\",\"province\":\"Shanghai\",\"country\":\"China\",\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/ouTZzarv7soYX1onplBgURkbd7sk087DXrRRS8X9zyytQnib8RbKeWdxXql0W5D8se6JCfuj5Oeqf5XbE7kmF7OdaN6DcOnYW/0\",\"createdOn\":1482670724000,\"shortName\":\"\",\"address\":\"\",\"description\":\"\",\"open\":true,\"takeByPhone\":true,\"takeByPhoneSuffix\":true,\"imageSource\":\"1896\",\"qrCode\":\"gQFk8TwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyZGJkRGxlblA5QlQxMDAwMGcwN1kAAgTl1NBYAwQAAAAA\",\"discountType\":1,\"discount\":0.9,\"amount\":0.02,\"takeOut\":true,\"categorys\":null,\"products\":null,\"carts\":null,\"openRanges\":null,\"introduce\":null},\"customer\":{\"id\":6362,\"loginName\":\"陈昊\",\"openId\":\"oV3Nlt5AxtolNeOWABMnMg0MK3e8\",\"name\":\"陈昊\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"cardNo\":null,\"cardUsed\":false,\"phone\":\"13817475684\",\"mail\":null,\"city\":\"Minhang\",\"province\":\"Shanghai\",\"country\":\"China\",\"address\":null,\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/HMKHw6icup2sWnA5TVHLotuhPhMrniaoASSFauM8kn0LW5r7624sLzJHE5cm2CrEfPEKjOibpj8fJpLbo50rK8VtJ8rib3vicUIuL/0\",\"account\":null,\"createdOn\":1491488163000,\"carts\":null,\"merchants\":null},\"status\":3,\"needPay\":false,\"totalPrice\":1.98,\"payTimeLimit\":0,\"payTime\":1496152317000,\"takeTimeLimit\":0,\"takeTime\":1496152317000,\"takeBeginTime\":1496152800000,\"takeEndTime\":1496159999000,\"createdOn\":1496152317000,\"updatedOn\":1496152317000,\"remark\":\"\",\"cartItems\":[{\"id\":3308142,\"name\":\"油条\",\"quantity\":1,\"unitPrice\":1.98,\"totalPrice\":1.98,\"product\":{\"id\":1921,\"name\":\"油条\",\"unitPrice\":2.00,\"description\":\"\",\"unitsInStock\":0,\"unitsInOrder\":2,\"infinite\":true,\"needPay\":false,\"openRange\":true,\"payTimeLimit\":10,\"takeTimeLimit\":0,\"imageSource\":null,\"createdOn\":1482672506000,\"updatedOn\":1482672506000,\"status\":0,\"category\":null,\"merchant\":{\"id\":1896,\"loginName\":\"陈昊\",\"openId\":\"oyA2pjnuf-oFDaZi0BwUlZ_XsX2U\",\"name\":\"陈昊\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"deviceNo\":\"36074020000430\",\"phone\":\"13817475681\",\"mail\":\"chenhao21@163.com\",\"city\":\"Minhang\",\"province\":\"Shanghai\",\"country\":\"China\",\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/ouTZzarv7soYX1onplBgURkbd7sk087DXrRRS8X9zyytQnib8RbKeWdxXql0W5D8se6JCfuj5Oeqf5XbE7kmF7OdaN6DcOnYW/0\",\"createdOn\":1482670724000,\"shortName\":\"\",\"address\":\"\",\"description\":\"\",\"open\":true,\"takeByPhone\":true,\"takeByPhoneSuffix\":true,\"imageSource\":\"1896\",\"qrCode\":\"gQFk8TwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyZGJkRGxlblA5QlQxMDAwMGcwN1kAAgTl1NBYAwQAAAAA\",\"discountType\":1,\"discount\":0.9,\"amount\":0.02,\"takeOut\":true,\"categorys\":null,\"products\":null,\"carts\":null,\"openRanges\":null,\"introduce\":null},\"openRanges\":[{\"id\":4194,\"beginTime\":null,\"endTime\":\"03:00:59\",\"products\":null},{\"id\":4356,\"beginTime\":\"08:00:00\",\"endTime\":\"09:30:59\",\"products\":null},{\"id\":4357,\"beginTime\":\"11:00:00\",\"endTime\":\"13:00:59\",\"products\":null},{\"id\":4358,\"beginTime\":\"22:00:00\",\"endTime\":\"23:59:59\",\"products\":null}],\"version\":76,\"takeNumber\":null,\"unTakeNumber\":null}}],\"version\":0,\"cardUsed\":true,\"payingNumber\":0}";

    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    static {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < MAX_WIDTH; i++) {
            sb.append("-");
        }
        DIVIDING_LINE = sb.toString();
    }

    public OrderTest() {

    }

    @PostConstruct
    public void init() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            public void run() {
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
                    //sb.append(cart.getId());
                    sb.append(count++);
                    // 数据分隔符
                    sb.append("*");
                    sb.append(getPrintOrderString(cart));
                    // 数据结束标示符
                    sb.append("#");

                    if (ChannelCache.INSTANCE.getChannel("1896") != null) {
                        byte[] bytes = sb.toString().getBytes("GBK");
                        ByteBuf byteBuf = Unpooled.buffer();
                        byteBuf.writeBytes(bytes);
                        ChannelCache.INSTANCE.getChannel("1896").writeAndFlush(byteBuf);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, 2, 30, TimeUnit.SECONDS);

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

        val += "*" + "商品名称" + getBlank(4) + "价格" + getBlank(3) + "数量" + getBlank(5) + "金额";
        for(CartItem item : cart.getCartItems()){
            String productName = item.getName();
            String name = productName.substring(0,  productName.length() > 13 ? 13 : productName.length());
            val += "*" + name;

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
        return getBlank(MAX_WIDTH-4 - s.length()) + s;
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
