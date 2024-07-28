package com.mashup.dojo.service

import com.mashup.dojo.DojoException
import com.mashup.dojo.DojoExceptionType
import com.mashup.dojo.MemberRelationEntity
import com.mashup.dojo.MemberRelationRepository
import com.mashup.dojo.domain.MemberId
import com.mashup.dojo.domain.MemberRelation
import com.mashup.dojo.domain.MemberRelationId
import com.mashup.dojo.domain.RelationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MemberRelationService {
    fun getAllRelationShip(fromId: String): List<MemberId>

    fun getFriendRelationIds(fromId: String): List<MemberId>

    fun getAccompanyRelationIds(fromId: String): List<MemberId>

    fun createRelation(
        fromId: MemberId,
        toId: MemberId,
    )

    fun updateRelationToFriend(
        fromId: String,
        toId: String,
    )
}

@Service
@Transactional(readOnly = true)
class DefaultMemberRelationService(
    private val memberRelationRepository: MemberRelationRepository,
) : MemberRelationService {
    override fun getAllRelationShip(fromId: String): List<MemberId> {
        return memberRelationRepository.findByFromId(fromId).map { MemberId(it) }
    }

    override fun getFriendRelationIds(fromId: String): List<MemberId> {
        return memberRelationRepository.findFriendsByFromId(fromId).map { MemberId(it) }
    }

    override fun getAccompanyRelationIds(fromId: String): List<MemberId> {
        return memberRelationRepository.findAccompanyByFromId(fromId).map { MemberId(it) }
    }

    @Transactional
    override fun createRelation(
        fromId: MemberId,
        toId: MemberId,
    ) {
        val memberRelation = MemberRelation.create(fromId, toId)
        memberRelationRepository.save(memberRelation.toEntity())
    }

    @Transactional
    override fun updateRelationToFriend(
        fromId: String,
        toId: String,
    ) {
        val toDomain = memberRelationRepository.findByFromIdAndToId(fromId, toId)?.toDomain() ?: throw DojoException.of(DojoExceptionType.FRIEND_NOT_FOUND)
        if (toDomain.relation == RelationType.FRIEND) {
            throw DojoException.of(DojoExceptionType.ALREADY_FRIEND)
        }
        val updatedRelation = toDomain.updateToFriend()
        memberRelationRepository.save(updatedRelation.toEntity())
    }
}

private fun MemberRelation.toEntity(): MemberRelationEntity {
    return MemberRelationEntity(
        id = id.value,
        fromId = fromId.value,
        toId = toId.value,
        relationType = com.mashup.dojo.RelationType.valueOf(relation.name)
    )
}

private fun MemberRelationEntity.toDomain(): MemberRelation {
    return MemberRelation(
        id = MemberRelationId(id),
        fromId = MemberId(fromId),
        toId = MemberId(toId),
        relation = com.mashup.dojo.domain.RelationType.valueOf(relationType.name),
        lastUpdatedAt = updatedAt
    )
}
