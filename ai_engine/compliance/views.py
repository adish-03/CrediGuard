import json
import os
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
import google.generativeai as genai

# =====================================================================
# 1. ENTERPRISE ML PIPELINE (Machine Learning)
# =====================================================================
print("Booting up AI Compliance Model...")
csv_path = os.path.join(settings.BASE_DIR, 'historical_data.csv')
df = pd.read_csv(csv_path)

X = df[['income', 'debt', 'age', 'missed_payments']] 
y = df['defaulted']

ai_model = RandomForestClassifier(random_state=42)
ai_model.fit(X, y)
print("✅ AI Model successfully trained on historical data!")

# =====================================================================
# NEW: GEN AI CONFIGURATION
# =====================================================================
# TODO: You will need to paste your free Google Gemini API key here!
genai.configure(api_key="YOUR_API_KEY_HERE")

@csrf_exempt
def analyze_risk(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            income = data.get('annualIncome', 1)
            debt = data.get('totalDebt', 0)
            age = data.get('age', 30)
            missed_payments = data.get('missed_payments', 0)
            applicant_name = data.get('applicantName', 'the applicant')

            customer_data = pd.DataFrame([[income, debt, age, missed_payments]], 
                                         columns=['income', 'debt', 'age', 'missed_payments'])

            # =====================================================================
            # 2. THE MATHEMATICIAN (Predictive Math)
            # =====================================================================
            probabilities = ai_model.predict_proba(customer_data)
            risk_probability = probabilities[0][1] 
            risk_score = round((risk_probability * 100), 2)

            # =====================================================================
            # 3. THE COMPLIANCE OFFICER (Generative AI)
            # =====================================================================
            is_compliant = True
            prompt = ""
            
            if risk_score > 40.00:
                is_compliant = False
                prompt = f"Act as a professional bank manager. Write a strict but polite 1-sentence rejection note for {applicant_name}. They have a {risk_score}% chance of defaulting based on our AI risk algorithm. Do not offer a second chance."
            else:
                prompt = f"Act as a cheerful bank manager. Write a 1-sentence approval note for {applicant_name}. Mention that their AI risk score is an excellent {risk_score}%."

            # Talk to Google Gemini!
            print("Checking Google servers for allowed AI models...")
            
            # DYNAMIC DISCOVERY FIX: Ask Google what models your specific key can access
            available_models = [m.name for m in genai.list_models() if 'generateContent' in m.supported_generation_methods]
            
            if not available_models:
                raise Exception("Your API key is valid, but your region/account has no text models available.")
            
            # Pick the fastest available model automatically
            flash_models = [m for m in available_models if 'flash' in m.lower()]
            chosen_model = flash_models[0] if flash_models else available_models[0]
            print(f"✅ Selected Model dynamically: {chosen_model}")

            llm = genai.GenerativeModel(chosen_model)
            gemini_response = llm.generate_content(prompt)
            compliance_note = gemini_response.text.strip()

            return JsonResponse({
                "status": "success",
                "risk_score": risk_score,
                "is_compliant": is_compliant,
                "compliance_note": compliance_note
            })
            
        except Exception as e:
            return JsonResponse({"status": "error", "message": str(e)}, status=400)
    
    return JsonResponse({"error": "Only POST requests are allowed."}, status=405)