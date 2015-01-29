package glbd;

import java.util.List;
import java.util.LinkedList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class Author {
    private long ID;
    private String name;
    private long alias_for;

    private static final String Q_AUTHOR =
        "SELECT name,alias_for_id FROM glb_author WHERE id=?";
    private static final String Q_AUTHOR_FIND =
        "SELECT id,name,alias_for_id FROM glb_author WHERE name LIKE ?";

    public Author(long p_ID, String p_name, long p_alias) {
        ID = p_ID;
        name = p_name;
        alias_for = p_alias;
    }

    public Author(SQLiteConnection conn, long p_ID) throws SQLiteException {
        SQLiteStatement st = conn.prepare(Q_AUTHOR);
        try {
            st.bind(1, p_ID);
            st.step();
            ID = p_ID;
            name = st.columnString(0);
            alias_for = st.columnLong(1);
        }
        finally {
            st.dispose();
        }
    }

    public String toString() {
        return name;
    }

    public long getID() {
        return ID;
    }

    public long getAlias() {
        return alias_for;
    }

    @Override
    public int hashCode() {
        return (int)ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        Author other = (Author)obj;
        return (ID == other.ID);
    }

    public static List<Author> find(SQLiteConnection conn,
                                    String author_pattern) throws SQLiteException {
        List<Author> alist = new LinkedList<Author>();
        if (!author_pattern.endsWith("%")) {
            author_pattern = author_pattern + "%";
        }
        SQLiteStatement st = conn.prepare(Q_AUTHOR_FIND);
        try {
            st.bind(1, author_pattern);
            while (st.step()) {
                Author a = new Author(st.columnLong(0),
                                      st.columnString(1),
                                      st.columnLong(2));
                alist.add(a);
            }
        }
        finally {
            st.dispose();
        }
        return alist;
    }
}
