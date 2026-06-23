from django.contrib import admin
from django.urls import path, include

# This is the Master Map for the entire Django Python server
urlpatterns = [
    path('admin/', admin.site.urls),
    
    # Route all traffic starting with 'api/v1/compliance/' to our new department
    path('api/v1/compliance/', include('compliance.urls')),
]