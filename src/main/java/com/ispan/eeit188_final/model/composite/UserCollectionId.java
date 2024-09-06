package com.ispan.eeit188_final.model.composite;

import java.io.Serializable;
import java.util.Objects;
// import java.util.UUID;

import com.ispan.eeit188_final.model.House;
import com.ispan.eeit188_final.model.User;

// import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class UserCollectionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "uniqueidentifier")
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", columnDefinition = "uniqueidentifier")
    private House houseId;

    // @Column(name = "user_id", columnDefinition = "uniqueidentifier")
    // private UUID userId;

    // @Column(name = "house_id", columnDefinition = "uniqueidentifier")
    // private UUID houseId;

    public UserCollectionId() {
    }

    public UserCollectionId(User userId, House houseId) {
        this.userId = userId;
        this.houseId = houseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserCollectionId that = (UserCollectionId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(houseId, that.houseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, houseId);
    }
}
