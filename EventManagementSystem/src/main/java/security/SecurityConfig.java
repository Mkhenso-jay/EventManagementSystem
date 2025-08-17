
package security;

import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;

@DataSourceDefinitions({
    @DataSourceDefinition(
        name = "java:app/jdbc/event_system",
        className = "com.mysql.cj.jdbc.MysqlDataSource",
        url = "jdbc:mysql://localhost:3306/event_system",
        user = "root",
        password = "MySQL2025#"
    )
})
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:app/jdbc/event_system",
    callerQuery = "SELECT password FROM users WHERE email = ?",
    groupsQuery = "SELECT role FROM users WHERE email = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class,
    priority = 30
)
public class SecurityConfig {}
