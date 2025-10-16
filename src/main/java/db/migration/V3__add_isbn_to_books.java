package db.migration;

import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V3__add_isbn_to_books extends BaseJavaMigration {
	@Override
	public void migrate(Context context) throws Exception {
		try (Statement stmt = context.getConnection().createStatement()) {
			stmt.execute("ALTER TABLE books ADD COLUMN isbn VARCHAR(32)");
		}
	}
}
