from django.urls import path
from . import views

# This is the map for the Compliance department
urlpatterns = [
    # When someone goes to /analyze/, send them to the Receptionist (analyze_risk view)
    path('analyze/', views.analyze_risk, name='analyze_risk'),
]