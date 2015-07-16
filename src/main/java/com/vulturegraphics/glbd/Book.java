package com.vulturegraphics.glbd;

import java.util.List;
import java.util.LinkedList;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;

class Book extends DBObject {
    private static final String X_BOOK_INSERT = "INSERT"
            + " INTO glb_book (title) VALUES(?)";
    private static final String Q_BOOK_FIND = "SELECT"
            + " _id,title FROM glb_book WHERE _id IN"
            + " (SELECT book_id FROM glb_book_author WHERE author_id=?)";
    private String title;

    private Book(long p_ID, String p_title) {
        super(p_ID);
        title = p_title;
    }

    /**
     * Add a title to the Book table
     * @param conn The connection object
     * @param title String title to add to database table
     * @return Book object
     * @throws SQLiteException
     */
    public static Book addTitle(SQLiteConnection conn,
                                String title) throws SQLiteException {
        SQLiteStatement st = conn.prepare(X_BOOK_INSERT);
        try {
            st.bind(1, title);
            st.step();
        } finally {
            st.dispose();
        }

        return new Book(conn.getLastInsertId(), title);
    }

    /**
     * Find books by a given author
     * @param conn The connection object
     * @param author Find books written by this author
     * @return List of books
     * @throws SQLiteException
     */
    public static List<Book> find(SQLiteConnection conn, Author author) throws SQLiteException {
        List<Book> bookList = new LinkedList<>();
        SQLiteStatement st = conn.prepare(Q_BOOK_FIND);
        try {
            st.bind(1, author.getID());
            while (st.step()) {
                Book b = new Book(st.columnLong(0), st.columnString(1));
                bookList.add(b);
            }
        } finally {
            st.dispose();
        }
        return bookList;

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
        Book other = (Book) obj;
        return (ID == other.ID && title.equals(other.title));
    }

    @Override
    public String toString() {
        return title;
    }
}
