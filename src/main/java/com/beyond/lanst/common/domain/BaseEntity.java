package com.beyond.lanst.common.domain;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    @Column(nullable = false, columnDefinition = "char(1) default 'N'")
    private String delYn = "N";

    public void updateDelYn(String del){
        this.delYn = del;
    }

}
