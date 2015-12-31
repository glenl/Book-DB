package com.vulturegraphics.glbd;

import java.util.List;
import java.util.LinkedList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author wraps an instance of a database table row representing a single author.
 */

class Author extends DBObject {
    private static final String Q_AUTHOR =
            "SELECT name,alias_for_id FROM glb_author WHERE _id=?";
    private static final String Q_AUTHOR_FIND =
            "SELECT _id,name,alias_for_id FROM glb_author WHERE name LIKE ?";
    private static final String Q_AUTHOR_FIND_EXACT =
            "SELECT _id,name,alias_for_id FROM glb_author WHERE name = ?";
    private static final String X_AUTHOR_INSERT =
            "INSERT INTO glb_author (name, alias_for_id) VALUES (?, ?)";
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
            if (st.step()) {
                name = st.columnString(0);
                alias_for = st.columnLong(1);
            }
        } finally {
            st.dispose();
        }
    }

    /**
     * Add an author (or alias) to the database. Author names have the form,
     *   <P>"LAST, FIRST"</P>
     *
     * @param conn The SQLite connection
     * @param author_name Name of author
     * @param alias_for If <code>author_name</code> is an alias, this is the author to which the alias refers.
     *                  If provided, the referenced author must already exist. May be null.
     * @return The created Author object, or null on any failure.
     * @throws SQLiteException May typically occur if a uniqueness constraint fails.
     */
    public static Author addAuthor(SQLiteConnection conn,
                                   String author_name,
                                   String alias_for) throws SQLiteException {
        Logger log = LoggerFactory.getLogger(Author.class);
        if (author_name == null) {
            return null;
        }
        if (alias_for == null) {
            SQLiteStatement st = conn.prepare(X_AUTHOR_INSERT);
            try {
                st.bind(1, author_name);
                st.bind(2, 0);
                if (st.step()) {
                    return new Author(st.columnLong(0),
                            st.columnString(1),
                            st.columnLong(2));
                }
                log.info("Added author \"" + author_name + "\"");
            } catch (SQLiteException ex) {
                log.warn(ex.getMessage());
            } finally {
                st.dispose();
            }
        }
        else {
            Author alias = Author.findExact(conn, alias_for);
            if (alias == null) {
                log.info("No alias found for " + alias_for + ", aborting author insertion.");
                return null;
            }
            SQLiteStatement st = conn.prepare(X_AUTHOR_INSERT);
            try {
                st.bind(1, author_name);
                st.bind(2, alias.getID());
                if (st.step()) {
                    return new Author(st.columnLong(0),
                            st.columnString(1),
                            st.columnLong(2));
                }
            } catch (SQLiteException ex) {
                log.warn(ex.getMessage());
            } finally {
                st.dispose();
            }
        }
        return null;
    }

    /**
     * Search the <code>glb_author</code> table for an <strong>exact</strong> match to an author's name.
     * @param conn The SQLite connection object.
     * @param author_name Name of author to match.
     * @return If found, the <code>Author</code> object filled in from the DB, null otherwise.
     * @throws SQLiteException on any error
     */
    public static Author findExact(SQLiteConnection conn,
                                   String author_name) throws SQLiteException {
        SQLiteStatement st = conn.prepare(Q_AUTHOR_FIND_EXACT);
        try {
            st.bind(1, author_name);
            if (st.step()) {
                return new Author(st.columnLong(0),
                        st.columnString(1),
                        st.columnLong(2));
            }
        } finally {
            st.dispose();
        }
        return null;
    }

    /**
     * Find all authors that begin with the given pattern. An SQL wildcard character (<code>%</code>) is always
     * appended to the pattern and the search is done with an SQL <code>LIKE</code> qualifier. This means that the
     * following samples will all return the same list:
     * <P>ROWLING</P>
     * <P>ROWLING%</P>
     * <P>ROWLING, J.</P>
     * @param conn The SQLite connection object
     * @param author_pattern Pattern to match
     * @return A list of matching authors. If none are found, this list is empty (<code>List.size() = 0</code>).
     * @throws SQLiteException on any error
     * @see #findExact(SQLiteConnection, String)
     */
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
    public String getName() {
        return name;
    }

    /**
     * A book can have many authors, an author can write many books. This M:M relationship is acchieved by a
     * mapping table, <code>glb_book_author</code>, which binds a single author to a single book. The same book
     * reference can be bound to other authors.
     * @param conn The SQLite connection
     * @param b The book  to add the database, associating to this Author instance
     * @return The row id of the new book entry
     * @throws SQLiteException Typically a constraint failure.
     */
    public long addBook(SQLiteConnection conn, Book b) throws SQLiteException {
        SQLiteStatement st = conn.prepare(X_BOOK_INSERT);
        long row_id = 0;
        try {
            st.bind(1, b.getID());
            st.bind(2, this.getID());
            st.step();
            row_id = conn.getLastInsertId();
        } finally {
            st.dispose();
        }
        return row_id;
    }

    /**
     * A convenience routine to find all books associated with this author.
     * @param conn The SQLite connection
     * @return A list of books by this author, possibly an empty list if no books found.
     * @throws SQLiteException Would be rare since you would have not been able to construct this Author
     * instance if it wasn't in the database.
     */
    public List<Book> bibliography(SQLiteConnection conn) throws SQLiteException {
        return Book.find(conn, this);
    }

}
