import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import { AuthProvider } from "./AuthContext";
import CustomerSignIn from "./pages/Authentication/CustomerSignIn";
import CustomerSignUp from "./pages/Authentication/CustomerSignUp";
import ProviderSignUp from "./pages/Authentication/ProviderSignUp";
import ProviderSignIn from "./pages/Authentication/ProviderSignIn";

function App() {
  return (
    <div>
      <AuthProvider>
        <Router>
          {/* <LocationFetcher /> */}
          <Routes>
            <Route path="/" element={<Home/>} />
            <Route path="/customer/login" element={<CustomerSignIn />} />
            <Route path="/customer/signup" element={<CustomerSignUp/>} />
            <Route path="/provider/login" element={<ProviderSignUp/>} />
            <Route path="/provider/signin" element={<ProviderSignIn/>} />
          </Routes>
        </Router>
      </AuthProvider>
    </div>
  );
}

export default App;
