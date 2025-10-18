package db.migration;

import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V4__add_bio_to_authors extends BaseJavaMigration {
	@Override
	public void migrate(Context context) throws Exception {
		try (Statement stmt = context.getConnection().createStatement()) {
			stmt.execute("ALTER TABLE authors ADD COLUMN bio VARCHAR(32)");
		}
	}
}
