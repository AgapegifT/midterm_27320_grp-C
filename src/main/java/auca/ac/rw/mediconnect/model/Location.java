package auca.ac.rw.mediconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String locationType;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String code;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"children", "hibernateLazyInitializer", "handler"})
    private Location parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Location> children = new ArrayList<>();
    
    @Column(name = "created_at")
    @JsonIgnore
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public Location() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Location getParent() { return parent; }
    public void setParent(Location parent) { this.parent = parent; }
    
    public List<Location> getChildren() { return children; }
    public void setChildren(List<Location> children) { this.children = children; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}