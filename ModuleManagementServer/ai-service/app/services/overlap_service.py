import os
import json
import logging
import numpy as np
import hashlib
from datetime import datetime
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from typing import List, Dict
from app.models.module import ModuleInfo
from app.models.overlap import SimilarModule

logger = logging.getLogger('uvicorn.error')

class OverlapService:
    def __init__(self, model_name='sentence-transformers/all-mpnet-base-v2', threshold=0.6):
        self.model_name = model_name
        self.threshold = threshold
        self.cache_dir = 'data/cache'
        self.json_dir = 'data/module-info'
        self.ensure_cache_dir()
        self.model = SentenceTransformer(model_name)
        self.embeddings = np.array([])
        self.modules = []
        self.load_default_modules()

    def ensure_cache_dir(self):
        if not os.path.exists(self.cache_dir):
            os.makedirs(self.cache_dir)

    def get_modules_hash(self, modules: List[Dict]) -> str:
        """Generate a hash of the modules data to detect changes"""
        # Sort modules by ID to ensure consistent hashing
        sorted_modules = sorted(modules, key=lambda x: x.get('module_id', ''))
        return hashlib.md5(json.dumps(sorted_modules, sort_keys=True).encode()).hexdigest()

    def load_cache(self) -> bool:
        """Load cached embeddings if they're still valid"""
        embeddings_file = os.path.join(self.cache_dir, 'embeddings.npz')
        metadata_file = os.path.join(self.cache_dir, 'metadata.json')
        
        if not (os.path.exists(embeddings_file) and os.path.exists(metadata_file)):
            return False

        try:
            # Load metadata
            with open(metadata_file, 'r') as f:
                metadata = json.load(f)
            
            # Load current modules to compare hash
            current_modules = self.load_modules_from_files()
            current_hash = self.get_modules_hash(current_modules)
            
            # Check if cache is still valid
            if (metadata['data_hash'] == current_hash and 
                metadata['model_name'] == self.model_name):
                # Load embeddings
                data = np.load(embeddings_file)
                self.embeddings = data['embeddings']
                self.modules = current_modules
                logger.info("Loaded valid cache")
                return True
                
        except Exception as e:
            logger.error(f"Error loading cache: {e}")
        
        return False

    def save_cache(self):
        """Save embeddings and metadata to cache"""
        logger.info("Saving to cache")
        embeddings_file = os.path.join(self.cache_dir, 'embeddings.npz')
        metadata_file = os.path.join(self.cache_dir, 'metadata.json')
        
        try:
            # Save embeddings
            np.savez_compressed(embeddings_file, embeddings=self.embeddings)
            
            # Save metadata
            metadata = {
                'data_hash': self.get_modules_hash(self.modules),
                'model_name': self.model_name,
                'timestamp': datetime.now().isoformat(),
                'num_modules': len(self.modules)
            }
            with open(metadata_file, 'w') as f:
                json.dump(metadata, f, indent=2)
                
            logger.info("Cache saved successfully")
        except Exception as e:
            logger.error(f"Error saving cache: {e}")

    def load_modules_from_files(self) -> List[Dict]:
        """Load modules from JSON files"""
        modules = []
        default_dir = os.path.join(self.json_dir)
        
        if not os.path.exists(default_dir):
            return modules

        for filename in os.listdir(default_dir):
            if filename.endswith('.json'):
                with open(os.path.join(default_dir, filename), 'r', encoding='utf-8') as f:
                    modules.extend(json.load(f))
        
        return modules

    def load_default_modules(self):
        """Load modules and compute/load embeddings"""
        logger.info("Loading modules")
        
        # Try to load from cache first
        if self.load_cache():
            return
            
        # If cache invalid or missing, load from files and compute new embeddings
        modules_data = self.load_modules_from_files()
        self.modules = modules_data
        
        # Compute new embeddings
        logger.info("Computing new embeddings")
        texts = [self.create_module_text(ModuleInfo(
            moduleId=m.get("module_id", ""),
            titleEng=m.get("title", ""),
            contentEng=m.get("content", ""),
            learningOutcomesEng=m.get("learning_outcomes", ""),
            examinationAchievementsEng=m.get("examination_achievements", ""),
            teachingMethodsEng=m.get("teaching_methods", ""),
            literatureEng=m.get("literature", ""),
            bulletPoints=""
        )) for m in modules_data]
        
        self.embeddings = self.model.encode(texts)
        self.save_cache()

    def create_module_text(self, module: ModuleInfo) -> str:
        weighted_fields = [
            module.titleEng * 2 if module.titleEng else "",
            module.contentEng if module.contentEng else "",
            module.learningOutcomesEng if module.learningOutcomesEng else "",
            module.examinationAchievementsEng if module.examinationAchievementsEng else "",
            module.teachingMethodsEng if module.teachingMethodsEng else "",
            module.literatureEng if module.literatureEng else ""
        ]
        return " ".join([f for f in weighted_fields if f])

    def find_similar_modules(self, module: ModuleInfo) -> List[SimilarModule]:
        logger.info("Finding similar module")
        if not self.modules:
            return []

        module_text = self.create_module_text(module)
        module_embedding = self.model.encode([module_text])[0]
        
        similarities = cosine_similarity([module_embedding], self.embeddings)[0]
        mask = similarities >= self.threshold
        similar_indices = np.where(mask)[0]
        
        results = [
            SimilarModule(
                moduleId=self.modules[idx]["module_id"],
                title=self.modules[idx]["title"],
                similarity=float(similarities[idx])
            )
            for idx in similar_indices
        ]

        logger.info(results)

        return sorted(results, key=lambda x: x.similarity, reverse=True)[:5]

    def add_module(self, module: ModuleInfo):
        """Add a new module and update embeddings"""
        module_dict = {
            "module_id": module.moduleId,
            "title": module.titleEng,
            "content": module.contentEng,
            "learning_outcomes": module.learningOutcomesEng,
            "examination_achievements": module.examinationAchievementsEng,
            "teaching_methods": module.teachingMethodsEng,
            "literature": module.literatureEng
        }
        self.modules.append(module_dict)
        
        module_text = self.create_module_text(module)
        new_embedding = self.model.encode([module_text])[0]
        
        if self.embeddings.size == 0:
            self.embeddings = new_embedding.reshape(1, -1)
        else:
            self.embeddings = np.vstack([self.embeddings, new_embedding])
        
        self.save_cache()