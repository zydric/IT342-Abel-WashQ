import { BrowserRouter } from 'react-router-dom';
import AppRoutes from './shared/routes/AppRoutes';

function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;
