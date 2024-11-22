export * from './feedback-controller.service';
import { FeedbackControllerService } from './feedback-controller.service';
export * from './feedback-controller.serviceInterface';
export * from './module-version-controller.service';
import { ModuleVersionControllerService } from './module-version-controller.service';
export * from './module-version-controller.serviceInterface';
export * from './proposal-controller.service';
import { ProposalControllerService } from './proposal-controller.service';
export * from './proposal-controller.serviceInterface';
export const APIS = [FeedbackControllerService, ModuleVersionControllerService, ProposalControllerService];
