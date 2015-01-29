package glbd;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

class GLBUtil {

    public void setAuthor(SQLiteConnection conn,
                          String author) throws SQLiteException {
        Logger log = LoggerFactory.getLogger(GLBUtil.class);
        List<Author> authors = Author.find(conn, author);
        if (authors.size() > 0) {
            curstate.set(authors.get(0));
            curstate.save(conn);
            log.info("author set to {}", authors.get(0));
        }
    }

    public void listBooksBy(SQLiteConnection conn,
                            String author_pattern) throws SQLiteException {
        List<Author> authors = Author.find(conn, author_pattern);
        Iterator<Author> a_iter = authors.iterator();
        while (a_iter.hasNext()) {
            Author a = a_iter.next();
            System.out.println(a);
            List<Book> books = Book.find(conn, a);
            Iterator<Book> b_iter = books.iterator();
            while (b_iter.hasNext()) {
                Book b = b_iter.next();
                System.out.println("   " + b);
            }
        }
    }

    /**
     * main - Setup application and run based on arguments
     */
    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.accepts("state", "Dump state info");
        parser.accepts("author", "Set state of current author")
            .withRequiredArg()
            .describedAs("author-name");
        parser.accepts("books-by", "List books by author")
            .withRequiredArg()
            .describedAs("author-name");
        parser.accepts("help", "this help message");

        OptionSet options;
        try {
            options = parser.parse(args);
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                return;
            }
        }
        catch (OptionException ex) {
            System.out.println(ex.getMessage());
            parser.printHelpOn(System.out);
            return;
        }

        // Set logging for this application.

        // Logging policy is purposely not set for the library but
        // users may want to use the following line since the sqlite
        // classes do a substantial amount of logging.
        java.util.logging.Logger.getLogger("com.almworks.sqlite4java")
            .setLevel(java.util.logging.Level.WARNING);

        // Start work
        final GLBUtil glbu = new GLBUtil();
        if (options.has("books-by")) {
            SQLiteQueue q = DBase.getInstance().getQueue();
            final String author = (String)options.valueOf("books-by");
            q.execute(new SQLiteJob<Void>() {
                    protected Void job(SQLiteConnection conn) throws SQLiteException {
                        glbu.listBooksBy(conn, author);
                        return null;
                    }
                }).complete();
            q.stop(true).join();
        }
        else if (options.has("author")) {
            SQLiteQueue q = DBase.getInstance().getQueue();
            final String author = (String)options.valueOf("author");
            q.execute(new SQLiteJob<Void>() {
                    protected Void job(SQLiteConnection conn) throws SQLiteException {
                        glbu.setAuthor(conn, author);
                        return null;
                    }
                }).complete();
            q.stop(true).join();
        }
        else if (options.has("state")) {
            SQLiteQueue q = DBase.getInstance().getQueue();
            State s = q.execute(new SQLiteJob<State>() {
                    protected State job(SQLiteConnection conn) throws SQLiteException {
                        return new State(conn);
                    }
                }).complete();
            q.stop(true).join();
            System.out.println("Current state is " + s);
        }
    }
}
