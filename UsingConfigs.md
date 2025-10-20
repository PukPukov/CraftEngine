# Инструкция по использованию конфигурации

## Инициализация конфига
Для создания конфига, нужно создать класс этого конфига и добавить для него аннотацию ```@Config```. В аннотации нужно указать названии файла, где будет находиться конфиг. Все поля в этом конфиге должны быть публичными и статическими, а также иметь аннотацию ```@ConfigField```. В аннотации нужно написать ключ для поля, который будет использоваться в конфиге. Поля обязательно должны иметь значение, оно будет использоваться в качестве стандартного Пример такого конфига выглядит так:

```java
@Config(name = "config.yml")
public class CraftEngineConfig {

    @ConfigField(name = "render-distance")
    public static int RENDER_DISTANCE = 8;

    @ConfigField(name = "fov")
    public static float FOV = 70.0f;

    @ConfigField(name = "fov-dynamic-multiplier")
    public static float FOV_DYNAMIC_MULTIPLIER = 1.0f;

    @ConfigField(name = "gui-scale")
    public static int guiScale = 6;

}
```

### Поля для списков
Поля со списками маркируются отдельной аннотацией ```@ListField```, потому что каждый элемент списка сериализируется и десериализируется отдельно. В аннотации нужно указать ключ, как для остальных полей, а также класс элементов в списке.

Пример:

```java

@Config(name = "config.yml")
public class CraftEngineConfig {

    @ListField(name = "test-list", type = Integer.class)
    public static List<Integer> TEST_LIST = List.Of(1, 2, 3, 4, 5);

}
```

### Категории

Для создания категории в уже имеющемся конфиге нужно объявить класс внутри конфига и выдать ему аннотацию ```@Config```, указав там ключ для каталога в конфиге. Выглядит это следующим образом:

```java
@Config(name = "config.yml")
public class CraftEngineConfig {

    @Config(name = "fov")
    public static class Fov {

        @ConfigField(name = "value")
        public static float VALUE = 70.0f;

        @ConfigField(name = "dynamic-multiplier")
        public static float DYNAMIC_MULTIPLIER = 1.0f;
        
    }

    @ConfigField(name = "render-distance")
    public static int RENDER_DISTANCE = 8;

    @ConfigField(name = "gui-scale")
    public static int guiScale = 6;

}
```

## ConfigManager

### Регистрация

Для регистрации конфигураций нужно создать экземпляр класса ConfigManager, это можно сделать через встроенный в него билдер следующим образом:

```java
ConfigManager configManager = ConfigManager.builder()
        .setParentFolder(new File("."))
        .setConfigs(CraftEngineConfig.class)
        .build();
```

В методе setParentFolder нужно указать папку, где будут находиться все конфиги, а в методе setConfigs нужно передать объекты классов конфигураций.

### Сохранение

Для сохранения есть метод saveConfigs, рекомендуется его вызывать при остановке игры:

```java
configManager.saveConfigs();
```

### Обновление конфига из файла во время игры

```java
configManager.reloadConfigs();
```

## Получение/назначение значений

Просто обращаемся к статическим полям конфига, при нужде изменить значение просто изменяем значение в поле и при ближайшем вызове метода saveConfigs данные перенесутся в файл конфигурации.
