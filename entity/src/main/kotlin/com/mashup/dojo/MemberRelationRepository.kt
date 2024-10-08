package com.mashup.dojo

import com.mashup.dojo.base.BaseTimeEntity
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRelationRepository : JpaRepository<MemberRelationEntity, String>, MemberRelationQueryRepository {
    fun findByFromIdAndToId(
        fromId: String,
        toId: String,
    ): MemberRelationEntity?
}

@Entity
@Table(name = "member_relation")
class MemberRelationEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,
    @Column(name = "from_id", nullable = false)
    val fromId: String,
    @Column(name = "to_id", nullable = false)
    val toId: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    val relationType: RelationType,
) : BaseTimeEntity()

enum class RelationType {
    FRIEND,
    ACCOMPANY,
}

interface MemberRelationQueryRepository {
    fun findByFromId(fromId: String): List<String>

    fun findFriendsByFromId(fromId: String): List<String>

    fun findAccompanyByFromId(fromId: String): List<String>

    fun isFriend(
        fromId: String,
        toId: String,
    ): Boolean

    fun findRandomOfFriend(
        memberId: String,
        limit: Long,
    ): List<String>

    fun findRandomOfAccompany(
        memberId: String,
        limit: Long,
    ): List<String>
}

class MemberRelationQueryRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : MemberRelationQueryRepository {
    override fun findByFromId(fromId: String): List<String> {
        val memberRelation = QMemberRelationEntity.memberRelationEntity

        return jpaQueryFactory
            .select(memberRelation.toId)
            .from(memberRelation)
            .where(
                memberRelation.fromId.eq(fromId)
            )
            .fetch()
    }

    override fun findFriendsByFromId(fromId: String): List<String> {
        return findByFromIdAndRelationType(fromId, RelationType.FRIEND)
    }

    override fun findAccompanyByFromId(fromId: String): List<String> {
        return findByFromIdAndRelationType(fromId, RelationType.ACCOMPANY)
    }

    override fun isFriend(
        fromId: String,
        toId: String,
    ): Boolean {
        val memberRelationEntity = QMemberRelationEntity.memberRelationEntity

        val findMemberRelation =
            jpaQueryFactory
                .selectFrom(memberRelationEntity)
                .where(
                    memberRelationEntity.fromId.eq(fromId),
                    memberRelationEntity.toId.eq(toId),
                    memberRelationEntity.relationType.eq(RelationType.FRIEND)
                )
                .fetchOne()

        return findMemberRelation != null
    }

    override fun findRandomOfFriend(
        memberId: String,
        limit: Long,
    ): List<String> {
        val memberRelation = QMemberRelationEntity.memberRelationEntity

        return jpaQueryFactory
            .select(memberRelation.toId)
            .from(memberRelation)
            .where(
                memberRelation.fromId.eq(memberId),
                memberRelation.relationType.eq(RelationType.FRIEND)
            )
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('RAND')").asc())
            .limit(limit)
            .fetch()
    }

    override fun findRandomOfAccompany(
        memberId: String,
        limit: Long,
    ): List<String> {
        val memberRelation = QMemberRelationEntity.memberRelationEntity

        return jpaQueryFactory
            .select(memberRelation.toId)
            .from(memberRelation)
            .where(
                memberRelation.fromId.eq(memberId),
                memberRelation.relationType.eq(RelationType.ACCOMPANY)
            )
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('RAND')").asc())
            .limit(8)
            .fetch()
    }

    private fun findByFromIdAndRelationType(
        fromId: String,
        relationType: RelationType,
    ): List<String> {
        val memberRelation = QMemberRelationEntity.memberRelationEntity

        return jpaQueryFactory
            .select(memberRelation.toId)
            .from(memberRelation)
            .where(
                memberRelation.fromId.eq(fromId),
                memberRelation.relationType.eq(relationType)
            )
            .fetch()
    }
}
