package me.civka.monopoly.common;

/** Time-limited or permanent effects attached to a member by projects/wonders. */
public enum AdditionalEffectType {
  // Portable now: gold-per-turn from commercial hub investment, by district level.
  COMMERCIAL_HUB_INVESTMENT_1,
  COMMERCIAL_HUB_INVESTMENT_2,
  COMMERCIAL_HUB_INVESTMENT_3,
  COMMERCIAL_HUB_INVESTMENT_4,
  // Deferred: require wonder-effect machinery that does not yet exist (see PROJECTS_PLAN.md).
  GOODY_HUT_WONDER_DISCOUNT,
  WONDER_DISCOUNT_1,
  WONDER_DISCOUNT_2,
  WONDER_DISCOUNT_3,
  WONDER_DISCOUNT_4,
  ALLIANCE
}
