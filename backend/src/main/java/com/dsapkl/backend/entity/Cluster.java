package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cluster")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cluster {
    
    @Id
    @Column(name = "cluster_id")
    private Integer id;
    
    @Column(name = "cluster_number")
    private Integer clusterNumber;

    public Cluster(Integer id, Integer clusterNumber) {
        this.id = id;
        this.clusterNumber = clusterNumber;
    }

    public void setClusterNumber(Integer clusterNumber) {
        this.clusterNumber = clusterNumber;
    }
}
