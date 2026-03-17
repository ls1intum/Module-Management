import { User } from '../modules/openapi';

/** Roles that can review proposals and see pending feedbacks (approval staff, program coordinators, specialization area responsibles). */
export const REVIEWER_ROLES: readonly User.RolesEnum[] = [
  User.RolesEnum.QualityManagement,
  User.RolesEnum.AcademicProgramAdvisor,
  User.RolesEnum.ExaminationBoard,
  User.RolesEnum.ProgramCoordinator,
  User.RolesEnum.SpecializationAreaResponsible
] as const;

export function isAdminRole(roles: User.RolesEnum[] | undefined | null): boolean {
  return Array.isArray(roles) && roles.includes(User.RolesEnum.Admin);
}

export function isProfessorRole(roles: User.RolesEnum[] | undefined | null): boolean {
  return Array.isArray(roles) && roles.includes(User.RolesEnum.Professor);
}

export function isReviewerRole(roles: User.RolesEnum[] | undefined | null): boolean {
  return Array.isArray(roles) && roles.some((r) => (REVIEWER_ROLES as readonly string[]).includes(r));
}
