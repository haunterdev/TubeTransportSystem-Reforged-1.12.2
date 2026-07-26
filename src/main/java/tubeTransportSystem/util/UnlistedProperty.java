package tubeTransportSystem.util;

import net.minecraftforge.common.property.IUnlistedProperty;

/** Minimal generic unlisted property for passing computed render data to a baked model. */
public class UnlistedProperty<T> implements IUnlistedProperty<T> {
    private final String name;
    private final Class<T> type;

    public UnlistedProperty(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String valueToString(T value) {
        return String.valueOf(value);
    }
}
