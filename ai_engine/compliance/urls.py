from django.urls import path
from . import views

# This is the map for the Compliance department
urlpatterns = [
    # 1. Normal customer workflow
    path('analyze/', views.analyze_risk, name='analyze_risk'),
    
    # 2. Manual trigger to wake the Agent up without waiting for midnight
    path('trigger-agent/', views.manual_trigger_agent, name='trigger_agent'),
    
    # 3. ADMIN DASHBOARD: View what the AI suggested
    path('pending-updates/', views.get_pending_updates, name='pending_updates'),
    
    # 4. ADMIN DASHBOARD: Approve or Reject the AI's suggestions
    path('approve-update/', views.approve_update, name='approve_update'),
]