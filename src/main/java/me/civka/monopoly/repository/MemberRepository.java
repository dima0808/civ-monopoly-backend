package me.civka.monopoly.repository;

import java.util.UUID;
import me.civka.monopoly.repository.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {}
