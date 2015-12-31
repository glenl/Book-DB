package com.vulturegraphics.glbd;

/**
 * Abstract class for all database objects.
 */
public abstract class DBObject {
    int ID;
    DBObject(int p_id) {
        ID = p_id;
    }
    final int getID() {
        return ID;
    }
}
