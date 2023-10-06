import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Authentication from "./pages/Authentication/Authentication";
import LocationFetcher from "./LocationFetcher";
import ServiceProviderSignUp from "./components/Authentication/ServiceProviderSignUp";
import ServiceProviderAuth from "./pages/Authentication/ServiceProviderAuth"

function App() {
  return (
    <div>
      <BrowserRouter>
        {/* <LocationFetcher /> */}
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/auth" element={<Authentication />} />
          <Route path="/provider/auth" element={<ServiceProviderAuth />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
