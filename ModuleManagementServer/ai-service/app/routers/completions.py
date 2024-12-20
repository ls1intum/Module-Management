from fastapi import APIRouter
from ..models.module import ModuleInfo, ContentGenerationResponse, ExaminationAchievementsGenerationResponse, LearningOutcomesGenerationResponse, TeachingMethodsGenerationResponse, MediaGenerationResponse
from ..services.llm_service import llm_service

router = APIRouter(prefix="/completions", tags=["completions"])

@router.post("/content", response_model=ContentGenerationResponse)
async def generate_content(module_info: ModuleInfo) -> ContentGenerationResponse:
    field = 'content'
    context = accumulate_module_info_for(field, module_info)
    prompt = get_prompt_for(field, module_info.bullet_points, context)
    content = await llm_service.generate_content(prompt)
    return ContentGenerationResponse(content=content)


@router.post("/examination-achievements", response_model=ExaminationAchievementsGenerationResponse)
async def generate_examination_achievements(module_info: ModuleInfo) -> ExaminationAchievementsGenerationResponse:
    field = 'examination-achievements'
    context = accumulate_module_info_for(field, module_info)
    prompt = get_prompt_for(field, module_info.bullet_points, context)
    examination_achievements = await llm_service.generate_content(prompt)
    return ExaminationAchievementsGenerationResponse(examination_achievement=examination_achievements)

@router.post("/learning-outcomes", response_model=LearningOutcomesGenerationResponse)
async def generate_learning_outcomes(module_info: ModuleInfo) -> LearningOutcomesGenerationResponse:
    field = 'learning-outcomes'
    context = accumulate_module_info_for(field, module_info)
    prompt = get_prompt_for(field, module_info.bullet_points, context)
    learning_outcomes = await llm_service.generate_content(prompt)
    return LearningOutcomesGenerationResponse(learning_outcomes=learning_outcomes)

@router.post("/teaching-methods", response_model=TeachingMethodsGenerationResponse)
async def generate_teaching_methods(module_info: ModuleInfo) -> TeachingMethodsGenerationResponse:
    field = 'teaching-methods'
    context = accumulate_module_info_for(field, module_info)
    prompt = get_prompt_for(field, module_info.bullet_points, context)
    teaching_methods = await llm_service.generate_content(prompt)
    return TeachingMethodsGenerationResponse(teaching_methods=teaching_methods)

@router.post("/media", response_model=MediaGenerationResponse)
async def generate_media(module_info: ModuleInfo) -> MediaGenerationResponse:
    field = 'media'
    context = accumulate_module_info_for(field, module_info)
    prompt = get_prompt_for(field, module_info.bullet_points, context)
    media = await llm_service.generate_content(prompt)
    return MediaGenerationResponse(media=media)

    
def accumulate_module_info_for(field: str, module_info: ModuleInfo):
    context_parts = []

    if module_info.title_eng:
        context_parts.append(f"Module Title: {module_info.title_eng}")
    if module_info.level_eng:
        context_parts.append(f"Level: {module_info.level_eng}")
    if module_info.language_eng:
        context_parts.append(f"Language: {module_info.language_eng.value}")
    if module_info.frequency_eng:
        context_parts.append(f"Frequency: {module_info.frequency_eng}")
    if module_info.credits:
        context_parts.append(f"Credits: {module_info.credits}")
    if module_info.hours_total:
        context_parts.append(f"Total Hours: {module_info.hours_total}")
    if module_info.hours_self_study:
        context_parts.append(f"Self Study Hours: {module_info.hours_self_study}")
    if module_info.hours_presence:
        context_parts.append(f"Presence Hours: {module_info.hours_presence}")
    if module_info.examination_achievements_eng and field != 'examination-achievements':
        context_parts.append(f"Examination Achievements: {module_info.examination_achievements_eng}")
    if module_info.repetition_eng:
        context_parts.append(f"Repetition: {module_info.repetition_eng}")
    if module_info.recommended_prerequisites_eng:
        context_parts.append(f"Recommended Prerequisites: {module_info.recommended_prerequisites_eng}")
    if module_info.content_eng and field != 'content':
        context_parts.append(f"Current Content: {module_info.content_eng}")
    if module_info.learning_outcomes_eng and field != 'learning-outcomes':
        context_parts.append(f"Learning Outcomes: {module_info.learning_outcomes_eng}")
    if module_info.teaching_methods_eng and field != 'teaching-methods':
        context_parts.append(f"Teaching Methods: {module_info.teaching_methods_eng}")
    if module_info.media_eng and field != 'media':
        context_parts.append(f"Media: {module_info.media_eng}")
    if module_info.literature_eng:
        context_parts.append(f"Literature: {module_info.literature_eng}")
    if module_info.responsibles_eng:
        context_parts.append(f"Responsibles: {module_info.responsibles_eng}")
    if module_info.lv_sws_lecturer_eng:
        context_parts.append(f"LV SWS Lecturer: {module_info.lv_sws_lecturer_eng}")
    return ' | '.join(context_parts)

def get_prompt_for(field: str, bullet_points: str, context: str):
    return f"""You are a professor at the Technical University of Munich and an expert in creating academic module descriptions.
Given the following information about a module:

{context}

And these key points about the module:
{bullet_points}

Generate a comprehensive, well-structured description of the module's {field}.
The description should:
- Be written in clear academic English
- Be detailed but concise
- Within 100-300 words but as little as necessary
- Follow a logical structure
- Maintain a professional tone
- Be suitable for a university module catalog

Generate only the {field} description, without any additional comments, explanations, or prepending the data with the name of the field."""