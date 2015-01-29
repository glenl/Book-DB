package glbd;

import java.util.List;
import java.util.LinkedList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class Book {
    private long ID;
    private String title;

    private static final String Q_BOOK_FIND =
        "SELECT id,title FROM glb_book WHERE id IN" +
        " (SELECT book_id FROM glb_book_author WHERE author_id=?)";

    public Book(long p_ID, String p_title) {
        ID = p_ID;
        title = p_title;
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
        Book other = (Book)obj;
        return (ID == other.ID && title.equals(other.title));
    }

    public String toString() {
        return title;
    }

    public static List<Book> find(SQLiteConnection conn,
                                  Author a) throws SQLiteException {
        List<Book> blist = new LinkedList<Book>();
        SQLiteStatement st = conn.prepare(Q_BOOK_FIND);
        try {
            st.bind(1, a.getID());
            while (st.step()) {
                Book b = new Book(st.columnLong(0),
                                  st.columnString(1));
                blist.add(b);
            }
        }
        finally {
            st.dispose();
        }
        return blist;
    }
}
