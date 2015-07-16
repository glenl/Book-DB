package com.vulturegraphics.glbd;

public abstract class DBObject {
    long ID;
    DBObject(long p_id) {
        ID = p_id;
    }
    final long getID() {
        return ID;
    }

    @Override
    public int hashCode() {
        return (int) ID;
    }
}
