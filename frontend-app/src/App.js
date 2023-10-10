import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Authentication from "./pages/Authentication/Authentication";
import LocationFetcher from "./LocationFetcher";
import ServiceProviderSignUp from "./components/Authentication/ProviderAuth/ServiceProviderSignUp";
import ServiceProviderAuth from "./pages/Authentication/ServiceProviderAuth"
import { AuthProvider } from "./AuthContext";

function App() {
  return (
    <div>
      <AuthProvider>
        <BrowserRouter>
          {/* <LocationFetcher /> */}
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/auth" element={<Authentication />} />
            <Route path="/provider/auth" element={<ServiceProviderAuth />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </div>
  );
}

export default App;
