from fastapi import APIRouter, HTTPException
from app.models.module import ModuleInfo, ContentGenerationResponse, ExaminationAchievementsGenerationResponse, LearningOutcomesGenerationResponse, TeachingMethodsGenerationResponse
from app.services.llm_service import llm_service

router = APIRouter(prefix="/completions", tags=["completions"])

@router.post("/content", response_model=ContentGenerationResponse)
async def generate_content(module_info: ModuleInfo) -> ContentGenerationResponse:
    try:
        field = 'content'
        context = accumulate_module_info_for(field, module_info)
        prompt = get_prompt_for(field, module_info.bulletPoints, module_info.contextPrompt, context)
        content = await llm_service.model.ainvoke(prompt)
        return ContentGenerationResponse(responseData=content.content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/examination-achievements", response_model=ExaminationAchievementsGenerationResponse)
async def generate_examination_achievements(module_info: ModuleInfo) -> ExaminationAchievementsGenerationResponse:
    try:
        field = 'examination-achievements'
        context = accumulate_module_info_for(field, module_info)
        prompt = get_prompt_for(field, module_info.bulletPoints, module_info.contextPrompt, context)
        examination_achievements = await llm_service.model.ainvoke(prompt)
        return ExaminationAchievementsGenerationResponse(responseData=examination_achievements.content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/learning-outcomes", response_model=LearningOutcomesGenerationResponse)
async def generate_learning_outcomes(module_info: ModuleInfo) -> LearningOutcomesGenerationResponse:
    try:
        field = 'learning-outcomes'
        context = accumulate_module_info_for(field, module_info)
        prompt = get_prompt_for(field, module_info.bulletPoints, module_info.contextPrompt, context)
        learning_outcomes = await llm_service.model.ainvoke(prompt)
        return LearningOutcomesGenerationResponse(responseData=learning_outcomes.content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/teaching-methods", response_model=TeachingMethodsGenerationResponse)
async def generate_teaching_methods(module_info: ModuleInfo) -> TeachingMethodsGenerationResponse:
    try:
        field = 'teaching-methods'
        context = accumulate_module_info_for(field, module_info)
        prompt = get_prompt_for(field, module_info.bulletPoints, module_info.contextPrompt, context)
        teaching_methods = await llm_service.model.ainvoke(prompt)
        return TeachingMethodsGenerationResponse(responseData=teaching_methods.content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
def accumulate_module_info_for(field: str, module_info: ModuleInfo):
    context_parts = []

    if module_info.titleEng:
        context_parts.append(f"Module Title: {module_info.titleEng}")
    if module_info.levelEng:
        context_parts.append(f"Level: {module_info.levelEng}")
    if module_info.languageEng:
        context_parts.append(f"Language: {module_info.languageEng.value}")
    if module_info.frequencyEng:
        context_parts.append(f"Frequency: {module_info.frequencyEng}")
    if module_info.credits:
        context_parts.append(f"Credits: {module_info.credits}")
    if module_info.hoursTotal:
        context_parts.append(f"Total Hours: {module_info.hoursTotal}")
    if module_info.hoursSelfStudy:
        context_parts.append(f"Self Study Hours: {module_info.hoursSelfStudy}")
    if module_info.hoursPresence:
        context_parts.append(f"Presence Hours: {module_info.hoursPresence}")
    if module_info.examinationAchievementsEng and field != 'examination-achievements':
        context_parts.append(f"Examination Achievements: {module_info.examinationAchievementsEng}")
    if module_info.repetitionEng:
        context_parts.append(f"Repetition: {module_info.repetitionEng}")
    if module_info.recommendedPrerequisitesEng:
        context_parts.append(f"Recommended Prerequisites: {module_info.recommendedPrerequisitesEng}")
    if module_info.contentEng and field != 'content':
        context_parts.append(f"Current Content: {module_info.contentEng}")
    if module_info.learningOutcomesEng and field != 'learning-outcomes':
        context_parts.append(f"Learning Outcomes: {module_info.learningOutcomesEng}")
    if module_info.teachingMethodsEng and field != 'teaching-methods':
        context_parts.append(f"Teaching Methods: {module_info.teachingMethodsEng}")
    if module_info.mediaEng and field != 'media':
        context_parts.append(f"Media: {module_info.mediaEng}")
    if module_info.literatureEng:
        context_parts.append(f"Literature: {module_info.literatureEng}")
    if module_info.responsiblesEng:
        context_parts.append(f"Responsibles: {module_info.responsiblesEng}")
    if module_info.lvSwsLecturerEng:
        context_parts.append(f"LV SWS Lecturer: {module_info.lvSwsLecturerEng}")
    return ' | '.join(context_parts)

def get_prompt_for(field: str, bullet_points: str, context_prompt: str, context: str):
    return f"""You are a professor at the Technical University of Munich and an expert in creating academic module descriptions.
Given the following information about a module:

{context}

And these key points about the module:
{bullet_points}

Generate a comprehensive, well-structured description of the module's {field}.
The description should:
- Be written in clear academic English
- Be detailed but concise
- Within 50-200 words but as little as necessary
- Follow a logical structure
- Maintain a professional tone
- Be suitable for a university module catalog
- Never mention Credits, Hours, Frequency, Duration unless it is asked for in the specific context

Consider all information, but without breaking the establised rules, specifically consider the following context:

{context_prompt}

Generate only the {field} description, without any additional comments, explanations, or prepending the data with the name of the field."""