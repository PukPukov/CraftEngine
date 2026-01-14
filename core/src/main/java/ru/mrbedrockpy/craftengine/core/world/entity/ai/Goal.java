package ru.mrbedrockpy.craftengine.core.world.entity.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.core.world.entity.Entity;

@Getter
@Setter
@AllArgsConstructor
public abstract class Goal {

    protected final Entity entity;
    @Nullable protected Vector3f targetPosition;

    public abstract void onStart();
    public abstract void onTick();
    public abstract void onChangeGoal(Goal newGoal);

    public final void tick() {
        this.onTick();
        if (this.targetPosition == null) return;
        // TODO: Сделать алгоритм поиска пути
    }

}
