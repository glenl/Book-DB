package com.vulturegraphics.glbd;

import java.util.List;
import java.util.LinkedList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;

class Author extends DBObject {
    private static final String Q_AUTHOR =
            "SELECT name,alias_for_id FROM glb_author WHERE _id=?";
    private static final String Q_AUTHOR_FIND =
            "SELECT _id,name,alias_for_id FROM glb_author WHERE name LIKE ?";
    private static final String X_BOOK_INSERT =
            "INSERT INTO glb_book_author(book_id,author_id) VALUES (?,?)";
    private long ID;
    private String name;
    private long alias_for;

    private Author(long p_ID, String p_name, long p_alias) {
        super(p_ID);
        name = p_name;
        alias_for = p_alias;
    }

    public Author(SQLiteConnection conn, long p_ID) throws SQLiteException {
        super(p_ID);
        SQLiteStatement st = conn.prepare(Q_AUTHOR);
        try {
            st.bind(1, p_ID);
            st.step();
            name = st.columnString(0);
            alias_for = st.columnLong(1);
        } finally {
            st.dispose();
        }
    }

    public static List<Author> find(SQLiteConnection conn,
                                    String author_pattern) throws SQLiteException {
        List<Author> alist = new LinkedList<>();
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
        } finally {
            st.dispose();
        }
        return alist;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Author other = (Author) obj;
        return (ID == other.ID);
    }

    public long getAlias() {
        return alias_for;
    }

    public long addBook(SQLiteConnection conn, Book b) throws SQLiteException {
        SQLiteStatement st = conn.prepare(X_BOOK_INSERT);
        try {
            st.bind(1, b.getID());
            st.bind(2, this.getID());
            st.step();
        } finally {
            st.dispose();
        }
        return conn.getLastInsertId();
    }

    public List<Book> bibliography(SQLiteConnection conn) throws SQLiteException {
        return Book.find(conn, this);
    }

}
