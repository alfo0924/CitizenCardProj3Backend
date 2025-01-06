package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount_store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "area", nullable = false)
    private String area;

    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "website")
    private String website;

    @Column(name = "category")
    private String category;

    @Column(name = "short_content")
    private String shortContent;

    @Column(name = "time")
    private String time;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "priority")
    private int priority;

    @Column(name = "popularity")
    private int popularity;

    @Column(name = "is_donation")
    private Boolean isDonation;

    @Column(name = "iframe_src")
    private String iframeSrc;

    @Column(name = "img_url")
    private String imgUrl;

}