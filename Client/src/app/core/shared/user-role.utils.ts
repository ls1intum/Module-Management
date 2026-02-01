import { User } from '../modules/openapi';

/** Roles that can review proposals (approval staff). */
export const REVIEWER_ROLES: readonly User.RoleEnum[] = [User.RoleEnum.QualityManagement, User.RoleEnum.AcademicProgramAdvisor, User.RoleEnum.ExaminationBoard] as const;

export function isAdminRole(role: User.RoleEnum | undefined | null): role is User.RoleEnum {
  return role === User.RoleEnum.Admin;
}

export function isProfessorRole(role: User.RoleEnum | undefined | null): role is User.RoleEnum {
  return role === User.RoleEnum.Professor;
}

export function isReviewerRole(role: User.RoleEnum | undefined | null): boolean {
  return role != null && (REVIEWER_ROLES as readonly string[]).includes(role);
}
