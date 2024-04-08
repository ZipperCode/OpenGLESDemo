package com.zipper.gldemo2;

import androidx.annotation.IntDef;

import java.util.Objects;

public class PaintRecord {

    public final int type;

    public final int drawColor;

    public final int areaColor;

    public final String path;

    public PaintRecord(int drawColor, int areaColor) {
        this.type = Type.COLOR;
        this.drawColor = drawColor;
        this.areaColor = areaColor;
        this.path = null;
    }

    public PaintRecord(int areaColor, String path) {
        this.type = Type.TEXTURE;
        this.drawColor = 0;
        this.areaColor = areaColor;
        this.path = path;
    }

    @IntDef({Type.COLOR, Type.TEXTURE})
    public @interface Type {
        int COLOR = 1;
        int TEXTURE = 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaintRecord that = (PaintRecord) o;
        return type == that.type && drawColor == that.drawColor && areaColor == that.areaColor && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, drawColor, areaColor, path);
    }
}
