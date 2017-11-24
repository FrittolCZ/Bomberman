package bomberman;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Brick {

    private BooleanProperty active;
    private IntegerProperty y;
    private IntegerProperty x;

    public Brick(int x, int y) {
        this.y = new SimpleIntegerProperty(y);
        this.x = new SimpleIntegerProperty(x);
        this.active = new SimpleBooleanProperty(true);
    }

    public final BooleanProperty activeProperty() {
        return this.active;
    }

    public final boolean isActive() {
        return this.activeProperty().get();
    }

    public final void setActive(final boolean active) {
        this.activeProperty().set(active);
    }

    public final void deactive() {
        this.active.set(false);
    }

    public final void active() {
        this.active.set(true);
    }

    public final IntegerProperty yProperty() {
        return this.y;
    }

    public final int getY() {
        return this.yProperty().get();
    }

    public final void setY(final int y) {
        this.yProperty().set(y);
    }

    public final IntegerProperty xProperty() {
        return this.x;
    }

    public final int getX() {
        return this.xProperty().get();
    }

    public final void setX(final int x) {
        this.xProperty().set(x);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.y);
        hash = 43 * hash + Objects.hashCode(this.x);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Brick)) {
            return false;
        }

        final Brick other = (Brick) obj;
        return !(other.getX() != this.getX() || other.getY() != this.getY());
    }

}
