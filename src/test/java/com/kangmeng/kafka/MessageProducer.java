package com.kangmeng.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kangmeng.model.order.Cart;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageProducer {

	private int count = 1000;

	private String cartJson = "{\"id\":3308137,\"no\":\"592d78fdb4c3eeb0136faef3\",\"transactionId\":null,\"merchant\":{\"id\":1896,\"loginName\":\"陈昊\",\"openId\":\"oyA2pjnuf-oFDaZi0BwUlZ_XsX2U\",\"name\":\"陈昊\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"deviceNo\":\"36074020000430\",\"phone\":\"13817475681\",\"mail\":\"chenhao21@163.com\",\"city\":\"Minhang\",\"province\":\"Shanghai\",\"country\":\"China\",\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/ouTZzarv7soYX1onplBgURkbd7sk087DXrRRS8X9zyytQnib8RbKeWdxXql0W5D8se6JCfuj5Oeqf5XbE7kmF7OdaN6DcOnYW/0\",\"createdOn\":1482670724000,\"shortName\":\"\",\"address\":\"\",\"description\":\"\",\"open\":true,\"takeByPhone\":true,\"takeByPhoneSuffix\":true,\"imageSource\":\"1896\",\"qrCode\":\"gQFk8TwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyZGJkRGxlblA5QlQxMDAwMGcwN1kAAgTl1NBYAwQAAAAA\",\"discountType\":1,\"discount\":0.9,\"amount\":0.02,\"takeOut\":true,\"categorys\":null,\"products\":null,\"carts\":null,\"openRanges\":null,\"introduce\":null},\"customer\":{\"id\":6362,\"loginName\":\"陈昊\",\"openId\":\"oV3Nlt5AxtolNeOWABMnMg0MK3e8\",\"name\":\"陈昊\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"cardNo\":null,\"cardUsed\":false,\"phone\":\"13817475684\",\"mail\":null,\"city\":\"Minhang\",\"province\":\"Shanghai\",\"country\":\"China\",\"address\":null,\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/HMKHw6icup2sWnA5TVHLotuhPhMrniaoASSFauM8kn0LW5r7624sLzJHE5cm2CrEfPEKjOibpj8fJpLbo50rK8VtJ8rib3vicUIuL/0\",\"account\":null,\"createdOn\":1491488163000,\"carts\":null,\"merchants\":null},\"status\":3,\"needPay\":false,\"totalPrice\":1.98,\"payTimeLimit\":0,\"payTime\":1496152317000,\"takeTimeLimit\":0,\"takeTime\":1496152317000,\"takeBeginTime\":1496152800000,\"takeEndTime\":1496159999000,\"createdOn\":1496152317000,\"updatedOn\":1496152317000,\"remark\":\"\",\"cartItems\":[{\"id\":3308142,\"name\":\"油条\",\"quantity\":1,\"unitPrice\":1.98,\"totalPrice\":1.98,\"product\":{\"id\":1921,\"name\":\"油条\",\"unitPrice\":2.00,\"description\":\"\",\"unitsInStock\":0,\"unitsInOrder\":2,\"infinite\":true,\"needPay\":false,\"openRange\":true,\"payTimeLimit\":10,\"takeTimeLimit\":0,\"imageSource\":null,\"createdOn\":1482672506000,\"updatedOn\":1482672506000,\"status\":0,\"category\":null,\"merchant\":{\"id\":1896,\"loginName\":\"陈昊\",\"openId\":\"oyA2pjnuf-oFDaZi0BwUlZ_XsX2U\",\"name\":\"陈昊\",\"password\":\"E10ADC3949BA59ABBE56E057F20F883E\",\"deviceNo\":\"36074020000430\",\"phone\":\"13817475681\",\"mail\":\"chenhao21@163.com\",\"city\":\"Minhang\",\"province\":\"Shanghai\",\"country\":\"China\",\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/ouTZzarv7soYX1onplBgURkbd7sk087DXrRRS8X9zyytQnib8RbKeWdxXql0W5D8se6JCfuj5Oeqf5XbE7kmF7OdaN6DcOnYW/0\",\"createdOn\":1482670724000,\"shortName\":\"\",\"address\":\"\",\"description\":\"\",\"open\":true,\"takeByPhone\":true,\"takeByPhoneSuffix\":true,\"imageSource\":\"1896\",\"qrCode\":\"gQFk8TwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyZGJkRGxlblA5QlQxMDAwMGcwN1kAAgTl1NBYAwQAAAAA\",\"discountType\":1,\"discount\":0.9,\"amount\":0.02,\"takeOut\":true,\"categorys\":null,\"products\":null,\"carts\":null,\"openRanges\":null,\"introduce\":null},\"openRanges\":[{\"id\":4194,\"beginTime\":null,\"endTime\":\"03:00:59\",\"products\":null},{\"id\":4356,\"beginTime\":\"08:00:00\",\"endTime\":\"09:30:59\",\"products\":null},{\"id\":4357,\"beginTime\":\"11:00:00\",\"endTime\":\"13:00:59\",\"products\":null},{\"id\":4358,\"beginTime\":\"22:00:00\",\"endTime\":\"23:59:59\",\"products\":null}],\"version\":76,\"takeNumber\":null,\"unTakeNumber\":null}}],\"version\":0,\"cardUsed\":true,\"payingNumber\":0}";

	private KafkaProducer producer;

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	public MessageProducer() {
		init();
	}

	public void start() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					Cart cart = objectMapper.readValue(cartJson, Cart.class);
					cart.setId(Long.parseLong("" + count));
					count++;

					String value = objectMapper.writeValueAsString(cart);
					ProducerRecord<String, String> record = new ProducerRecord<>(cart.getMerchant().getId().toString(),
							value);
					producer.send(record);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}, 2, 10, TimeUnit.SECONDS);
	}

	public void init() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producer = new KafkaProducer<String, String>(kafkaProps);
	}

	public static void main(String[] args) {
		MessageProducer messageProducer = new MessageProducer();
		messageProducer.start();
	}

}
