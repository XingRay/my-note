## jackson入门





### 1. 引入依赖



### 2. 基本使用



### 3. 高级功能



#### 3.1 字段特殊值转化

如果某个字段的类型是数值类型，如Long，该字段有值时服务器返回正常值，没有值时服务器返回特定字符，比如"-"，可以自定义反序列化器处理，如下：

```java
SimpleModule module = new SimpleModule();


module.addDeserializer(Long.class, new JsonDeserializer<Long>() {

	@Override

	public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {


		long valueAsLong = jsonParser.getValueAsLong();

        System.out.println("valueAsLong:" + valueAsLong);


		String valueAsString = jsonParser.getValueAsString();

		System.out.println("valueAsString:" + valueAsString);

		if (valueAsString.equals("-")) {

			return null;

		}


		return valueAsLong;

	}

});

        

ObjectMapper objectMapper = new ObjectMapper();

objectMapper.registerModule(module);
```

