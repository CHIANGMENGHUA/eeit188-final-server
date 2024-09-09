package com.ispan.eeit188_final.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "house")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID id;

    // 房源基本資訊
    @Column(name = "name", columnDefinition = "VARCHAR(50)")
    private String name; // 房源名稱
    @Column(name = "category", columnDefinition = "VARCHAR(15)")
    private String category; // 類型
    @Column(name = "information", columnDefinition = "VARCHAR(MAX)")
    private String information; // 詳細資訊
    @Column(name = "latitude_x", columnDefinition = "FLOAT")
    private Double latitudeX; // 經度
    @Column(name = "longitude_y", columnDefinition = "FLOAT")
    private Double longitudeY; // 緯度
    @Column(name = "country", columnDefinition = "VARCHAR(50)")
    private String country; // 國家
    @Column(name = "city", columnDefinition = "VARCHAR(50)")
    private String city; // 縣市
    @Column(name = "region", columnDefinition = "VARCHAR(50)")
    private String region; // 區域
    @Column(name = "address", columnDefinition = "VARCHAR(50)")
    private String address; // 地址
    @Column(name = "price", columnDefinition = "INT")
    private Integer price; // 初始價格

    // 房源基本設施 幾廳 幾房 幾衛 幾浴 (TinyInt)
    @Column(name = "living_dining_room", columnDefinition = "TINYINT")
    private Short livingDiningRoom; // 客廳
    @Column(name = "bedroom", columnDefinition = "TINYINT")
    private Short bedroom; // 房間
    @Column(name = "restroom", columnDefinition = "TINYINT")
    private Short restroom; // 衛生間 (馬桶)
    @Column(name = "bathroom", columnDefinition = "TINYINT")
    private Short bathroom; // 淋浴間

    // 常態設施 (True/False)
    @Column(name = "kitchen", columnDefinition = "BIT")
    private Boolean kitchen; // 廚房
    @Column(name = "balcony", columnDefinition = "BIT")
    private Boolean balcony; // 陽台

    // 狀態
    @Column(name = "show", columnDefinition = "BIT")
    private Boolean show; // 是否刊登顯示

    // 建立/修改 時間
    @Column(name = "create_at", columnDefinition = "DATETIME2")
    private Timestamp createdAt; // 建立時間
    @Column(name = "update_at", columnDefinition = "DATETIME2")
    private Timestamp updatedAt; // 修改時間

    // 關聯 房源的價格範圍
    @OneToMany(mappedBy = "house", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PriceRange> priceRanges = new HashSet<>();

    // 關聯 房源的附加設施
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "house_postulate", joinColumns = @JoinColumn(name = "house_id"), inverseJoinColumns = @JoinColumn(name = "postulate_id"))
    @Builder.Default
    private Set<Postulate> postulates = new HashSet<>();

    // 與 User 的關聯
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, columnDefinition = "UNIQUEIDENTIFIER")
    @JsonIgnore
    private User user;

    // 與 UserCollection 的關聯
    @OneToMany(mappedBy = "userCollectionId.houseId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<UserCollection> userCollections = new HashSet<>();

    // 自訂序列化 collectionCount
    @JsonProperty("collectionCount")
    public Integer collectionCount() {
        return userCollections != null ? userCollections.size() : null;
    }

    // 與 TransactionRecord 的關聯
    @OneToMany(mappedBy = "house", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<TransactionRecord> transactionRecords = new HashSet<>();

    // 自訂序列化 userId
    @JsonProperty("userId")
    public UUID userId() {
        return user != null ? user.getId() : null;
    }

    // 自訂序列化 userName
    @JsonProperty("userName")
    public String userName() {
        return user != null ? user.getName() : null;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

}
