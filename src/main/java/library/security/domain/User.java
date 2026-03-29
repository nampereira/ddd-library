package library.security.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "library_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(nullable = false, unique = true)
    private UUID userId = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "library_user_roles", joinColumns = @JoinColumn(name = "user_pk"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    protected User() {}

    public User(String username, String password, Set<Role> roles) {
        Assert.hasText(username, "username must not be blank");
        Assert.hasText(password, "password must not be blank");
        Assert.notNull(roles, "roles must not be null");
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public Long getPk() { return pk; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return roles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
