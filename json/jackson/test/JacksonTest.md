```java
public class Student {
    private Long id;
    private String name;
    private int age;

    private Score score;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", score=" + score +
                '}';
    }

    public static class Score {
        private String name;
        private Double value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Score{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}

```

```java
public class JacksonTest {
    @Test
    public void test01() {
        String json = "{\"id\":11112222, \"name\":\"jack\", \"age\":11}";
        Student student = null;
        try {
            student = new ObjectMapper().readValue(json, Student.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println(student);
    }

    @Test
    public void test02() {
        String json = "{\"id\":\"-\", \"name\":\"jack\", \"age\":11, \"score\":{\"name\":\"eng\", \"value\":\"-\"}}";
        String json2 = "{\"id\":111222, \"name\":\"rose\", \"age\":13, \"score\":{\"name\":\"math\", \"value\":22.34}}";

        try {
            ObjectMapper objectMapper = getObjectMapper();

            Student student = objectMapper.readValue(json, Student.class);
            System.out.println("student:" + student);
            Student student2 = objectMapper.readValue(json2, Student.class);
            System.out.println("student2:" + student2);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

   
    @NotNull
    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = getSimpleModule();
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @NotNull
    private static SimpleModule getSimpleModule() {
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

        module.addDeserializer(Integer.class, new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                String valueAsString = jsonParser.getValueAsString();
                if (valueAsString.equals("-")) {
                    return null;
                }
                return jsonParser.getValueAsInt();
            }
        });

        module.addDeserializer(Double.class, new JsonDeserializer<Double>() {
            @Override
            public Double deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                String valueAsString = jsonParser.getValueAsString();
                if (valueAsString.equals("-")) {
                    return null;
                }
                return jsonParser.getValueAsDouble();
            }
        });
        return module;
    }
}
```

