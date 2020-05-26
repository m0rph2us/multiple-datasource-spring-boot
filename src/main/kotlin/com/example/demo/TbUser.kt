package com.example.demo

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tb_user")
data class TbUser(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        val id: Long?,

        @Column(name = "user_id")
        val userId: String,

        @Column(name = "name")
        val name: String,

        @Column(name = "email")
        val email: String,

        @Column(name = "status")
        val status: Int,

        @Column(name = "delete_yn")
        val deleteYn: String,

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "reg_dt")
        val regDt: Date,

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "chg_dt")
        val chgDt: Date
)