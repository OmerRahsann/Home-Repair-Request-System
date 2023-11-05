import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Customer/Home";
import { AuthProvider } from "./AuthContext";
import CustomerSignIn from "./pages/Authentication/CustomerSignIn";
import CustomerSignUp from "./pages/Authentication/CustomerSignUp";
import ProviderSignUp from "./pages/Authentication/ProviderSignUp";
import ProviderSignIn from "./pages/Authentication/ProviderSignIn";
import RequestView from "./pages/Provider/RequestView"
import MyRequests from "./pages/Customer/MyRequests";

function App() {
  return (
    <div>
      <AuthProvider>
        <Router>
          {/* <LocationFetcher /> */}
          <Routes>
            <Route path="/" element={<Home/>} />
            <Route path="/customer/login" element={<CustomerSignIn />} /> //thats prob why we are getting the 404 
            <Route path="/customer/signup" element={<CustomerSignUp/>} />
            <Route path="/customer/myrequests" element={<MyRequests/>}/>
            <Route path="/provider/signup" element={<ProviderSignUp/>} />
            <Route path="/provider/login" element={<ProviderSignIn/>} />
            <Route path="/provider/viewrequests" element={<RequestView/>} />
            
          </Routes>
        </Router>
      </AuthProvider>
    </div>
  );
}

export default App;
