package glbd;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;

class State {
    private Author author;

    private static final String Q_STATE =
        "SELECT current_author FROM glb_state WHERE id=1";
    private static final String Q_SAVE =
        "UPDATE glb_state SET current_author=?";

    public State(SQLiteConnection conn) throws SQLiteException {
        author = null;
        SQLiteStatement st = conn.prepare(Q_STATE, true);
        try {
            st.step();
            author = new Author(conn, st.columnLong(0));
        }
        finally {
            st.dispose();
        }
    }

    public void set(Author new_author) {
        if (new_author != null) {
            author = new_author;
        }
    }

    public void save(SQLiteConnection conn) throws SQLiteException {
        SQLiteStatement st = conn.prepare(Q_SAVE);
        try {
            st.bind(1, author.getID());
            st.step();
        }
        finally {
            st.dispose();
        }
    }

    public String toString() {
        return author.toString();
    }
}
