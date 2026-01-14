package ru.mrbedrockpy.craftengine.client;

public class Main {

    // TODO: сделать нормальное освещение
    // TODO: сделать систему Entity: ИИ, рендеринг и тд
    // TODO: Сделать поддержку Generic моделей из BlockBench

    public static void main(String[] args) {
        try {
            CraftEngineClient.INSTANCE.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}