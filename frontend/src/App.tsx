import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';

import MainLayout from './layouts/MainLayout';
import AdminLayout from './layouts/AdminLayout';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PropertyListPage from './pages/PropertyListPage';
import PropertyDetailsPage from './pages/PropertyDetailsPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import AdminRentalsPage from './pages/AdminRentalsPage';
import UserRentalsPage from './pages/UserRentalsPage';
import NotFoundPage from './pages/NotFoundPage';

import './services/axiosConfig';

const ProtectedRoute = ({ children, requireAdmin = false }: { children: React.ReactNode, requireAdmin?: boolean }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  
  if (loading) {
    return <div className="flex items-center justify-center h-screen">Carregando...</div>;
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }
  
  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" />;
  }
  
  return <>{children}</>;
};

function App() {
  return (
    <Routes>
      {}
      <Route path="/" element={<MainLayout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="cadastro" element={<RegisterPage />} />
        <Route path="propriedades" element={<PropertyListPage />} />
        <Route path="propriedades/:id" element={<PropertyDetailsPage />} />
        
        {}
        <Route path="meus-alugueis" element={
          <ProtectedRoute>
            <UserRentalsPage />
          </ProtectedRoute>
        } />
      </Route>
      
      {}
      <Route path="/admin" element={
        <ProtectedRoute requireAdmin={true}>
          <AdminLayout />
        </ProtectedRoute>
      }>
        <Route index element={<AdminDashboardPage />} />
        <Route path="alugueis" element={<AdminRentalsPage />} />
      </Route>
      
      {}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default App;