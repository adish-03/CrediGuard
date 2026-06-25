import json
import os
import pandas as pd
from typing import TypedDict
from sklearn.ensemble import RandomForestClassifier
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
import google.generativeai as genai
from langgraph.graph import StateGraph, END
from apscheduler.schedulers.background import BackgroundScheduler
from dotenv import load_dotenv

# =====================================================================
# 1. ENTERPRISE ML PIPELINE & RULES SYSTEM
# =====================================================================
print("Booting up AI Compliance Model...")
csv_path = os.path.join(settings.BASE_DIR, 'historical_data.csv')
df = pd.read_csv(csv_path)

ai_model = RandomForestClassifier(random_state=42)
ai_model.fit(df[['income', 'debt', 'age', 'missed_payments']], df['defaulted'])

# BANK REGULATION MEMORY (State)
active_rules = {
    "max_debt_ratio": 40.0,
    "max_dependents": 5,
    "max_loan_limit": 500000.0
}
# Where the Agent drops its suggestions for the Admin
pending_agent_suggestions = {}

# =====================================================================
# 2. GEN AI CONFIGURATION (SECURE ENTERPRISE SETUP)
# =====================================================================
# Load hidden variables from the .env file
load_dotenv()

# Securely grab the key without hardcoding it in this file!
api_key = os.getenv("GEMINI_API_KEY")
if not api_key:
    print("⚠️ WARNING: GEMINI_API_KEY is missing from your .env file!")
else:
    genai.configure(api_key=api_key)

# =====================================================================
# 3. LANGGRAPH AGENTIC AI (The RBI/SEBI Scout)
# =====================================================================
class RegulatoryAgentState(TypedDict):
    regulator_data: str
    suggested_debt_ratio: float
    suggested_dependents: int
    suggested_loan_limit: float
    agent_reasoning: str

def scrape_regulators_node(state: RegulatoryAgentState):
    print("Agent: Scanning RBI and SEBI midnight circulars...")
    # Mocking the scraped data from both sites
    mock_data = """
    RBI Circular 12: Due to inflation, unsecured loan debt-to-income limits must cap at 30.0%.
    SEBI Guideline: High-risk individuals with more than 3 financial dependents should be restricted.
    Treasury limit on standard retail loans drops to 300,000.
    """
    return {"regulator_data": mock_data}

def analyze_regulations_node(state: RegulatoryAgentState):
    print("Agent: Using Gemini to analyze regulatory changes...")
    
    try:
        available_models = [m.name for m in genai.list_models() if 'generateContent' in m.supported_generation_methods]
        flash_models = [m for m in available_models if 'flash' in m.lower()]
        llm = genai.GenerativeModel(flash_models[0] if flash_models else available_models[0])

        prompt = f"""
        You are a Chief Risk Officer AI. Read these new regulations: '{state['regulator_data']}'
        Extract the 3 new mandated limits. Return EXACTLY this JSON and nothing else:
        {{"debt_ratio": 30.0, "dependents": 3, "loan_limit": 300000.0, "reasoning": "brief summary of changes"}}
        """
        
        response = llm.generate_content(prompt)
        
        clean_json = response.text.replace("```json", "").replace("```", "").strip()
        parsed = json.loads(clean_json)
        return {
            "suggested_debt_ratio": parsed.get("debt_ratio", 40.0),
            "suggested_dependents": parsed.get("dependents", 5),
            "suggested_loan_limit": parsed.get("loan_limit", 500000.0),
            "agent_reasoning": parsed.get("reasoning", "Parsed successfully")
        }
    except Exception as e:
        print("Agent Parsing Error:", e)
        return {"suggested_debt_ratio": 40.0, "suggested_dependents": 5, "suggested_loan_limit": 500000.0, "agent_reasoning": f"Google API Error: {str(e)}"}

workflow = StateGraph(RegulatoryAgentState)
workflow.add_node("scrape", scrape_regulators_node)
workflow.add_node("analyze", analyze_regulations_node)
workflow.set_entry_point("scrape")
workflow.add_edge("scrape", "analyze")
workflow.add_edge("analyze", END)
regulatory_agent = workflow.compile()

# =====================================================================
# 4. BACKGROUND SCHEDULER (The Midnight Clock)
# =====================================================================
def midnight_agent_task():
    global pending_agent_suggestions
    print("\n--- 🌙 MIDNIGHT CRON WAKING UP LANGGRAPH AGENT ---")
    final_state = regulatory_agent.invoke({"regulator_data": ""})
    
    # Store in the pending folder instead of active!
    pending_agent_suggestions = {
        "max_debt_ratio": final_state['suggested_debt_ratio'],
        "max_dependents": final_state['suggested_dependents'],
        "max_loan_limit": final_state['suggested_loan_limit'],
        "reasoning": final_state['agent_reasoning'],
        "status": "WAITING_FOR_ADMIN_APPROVAL"
    }
    print("--- 🛑 AGENT FINISHED: Suggestions sent to Admin Dashboard ---")

# Start the background clock
scheduler = BackgroundScheduler()
# This triggers every day at exactly 00:00 (Midnight)
scheduler.add_job(midnight_agent_task, 'cron', hour=0, minute=0)
scheduler.start()

# =====================================================================
# 5. DJANGO ENDPOINTS (Admin & Applications)
# =====================================================================
@csrf_exempt
def manual_trigger_agent(request):
    """ Since we don't want to wait until midnight, the Admin can manually force a scan """
    if request.method == 'POST':
        midnight_agent_task()
        return JsonResponse({"status": "Agent manually triggered. Suggestions are pending approval."})
    
    return JsonResponse({"error": "Please change your Thunder Client method to POST to trigger the agent."}, status=405)

@csrf_exempt
def get_pending_updates(request):
    """ The Admin Dashboard loads this to see what the AI found """
    if request.method == 'GET':
        return JsonResponse({
            "active_rules": active_rules,
            "pending_suggestions": pending_agent_suggestions
        })
    return JsonResponse({"error": "Please change your Thunder Client method to GET."}, status=405)

@csrf_exempt
def approve_update(request):
    """ The Admin clicks 'Approve' or 'Reject' """
    global active_rules, pending_agent_suggestions
    if request.method == 'POST':
        try:
            if not request.body:
                return JsonResponse({"error": "Missing JSON body! Please add {\"decision\": \"approve\"} in Thunder Client's Body tab."}, status=400)
                
            data = json.loads(request.body)
            decision = data.get('decision', 'reject') 
            
            if decision == 'approve' and pending_agent_suggestions:
                active_rules["max_debt_ratio"] = pending_agent_suggestions["max_debt_ratio"]
                active_rules["max_dependents"] = pending_agent_suggestions["max_dependents"]
                active_rules["max_loan_limit"] = pending_agent_suggestions["max_loan_limit"]
                pending_agent_suggestions.clear()
                return JsonResponse({"status": "SUCCESS: Active bank rules officially updated!"})
                
            pending_agent_suggestions.clear()
            return JsonResponse({"status": "REJECTED: Agent suggestions trashed. Keeping old rules."})
            
        except json.JSONDecodeError:
            return JsonResponse({"error": "Invalid JSON format. Check for missing double quotes!"}, status=400)
        except Exception as e:
            return JsonResponse({"error": f"Server crash prevented. Reason: {str(e)}"}, status=500)
            
    return JsonResponse({"error": "Please change your Thunder Client method to POST."}, status=405)

@csrf_exempt
def analyze_risk(request):
    """ Standard application check """
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            
            # 1. Grab all the data from the request (including the new ones)
            income = data.get('annualIncome', 1)
            debt = data.get('totalDebt', 0)
            age = data.get('age', 30)
            missed_payments = data.get('missed_payments', 0)
            applicant_name = data.get('applicantName', 'the applicant')
            dependents = data.get('dependents', 0)
            loan_amount = data.get('loanAmount', 0.0)

            # 2. Run the actual Machine Learning Model
            customer_data = pd.DataFrame([[income, debt, age, missed_payments]], 
                                         columns=['income', 'debt', 'age', 'missed_payments'])

            probabilities = ai_model.predict_proba(customer_data)
            risk_score = round((probabilities[0][1] * 100), 2)
            
            is_compliant = True
            prompt = ""
            
            # 3. ENFORCING THE APPROVED RULES FROM THE AGENT
            if risk_score > active_rules["max_debt_ratio"]:
                is_compliant = False
                prompt = f"Write a professional rejection for {applicant_name} for violating the {active_rules['max_debt_ratio']}% risk rule."
            elif dependents > active_rules["max_dependents"]:
                is_compliant = False
                prompt = f"Write a professional rejection for {applicant_name}. They have {dependents} dependents, which violates the maximum limit of {active_rules['max_dependents']}."
            elif loan_amount > active_rules["max_loan_limit"]:
                is_compliant = False
                prompt = f"Write a professional rejection for {applicant_name}. They requested ${loan_amount}, exceeding our strict bank limit of ${active_rules['max_loan_limit']}."
            else:
                prompt = f"Write a cheerful approval for {applicant_name}. Mention their excellent AI risk score of {risk_score}%."

            # 4. Talk to Gemini
            available_models = [m.name for m in genai.list_models() if 'generateContent' in m.supported_generation_methods]
            flash_models = [m for m in available_models if 'flash' in m.lower()]
            chosen_model = flash_models[0] if flash_models else available_models[0]
            llm = genai.GenerativeModel(chosen_model)

            gemini_response = llm.generate_content(prompt)

            # 5. Send ALL data back to Java!
            return JsonResponse({
                "status": "success", 
                "compliant": is_compliant, 
                "compliance_note": gemini_response.text.strip(),
                "risk_score": risk_score,
                "prompt_sent_to_ai": prompt
            })
        except Exception as e:
            return JsonResponse({"status": "error", "message": str(e)}, status=400)
    return JsonResponse({"error": "Only POST requests are allowed."}, status=405)