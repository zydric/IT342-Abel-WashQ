import React from 'react';

const STATUS_STAGES = [
  { id: 'PENDING', label: 'Pending', order: 1 },
  { id: 'RECEIVED', label: 'Received', order: 2 },
  { id: 'IN_PROGRESS', label: 'In Progress', order: 3 },
  { id: 'READY_FOR_PICKUP', label: 'Ready for Pickup', order: 4 },
  { id: 'COMPLETED', label: 'Completed', order: 5 },
];

export default function StatusTimeline({ currentStatus }) {
  if (currentStatus === 'CANCELLED') {
    return (
      <div className="w-full bg-red-100 border border-red-200 text-red-700 p-4 rounded-lg flex items-center justify-center gap-2">
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        <span className="font-medium text-sm">This booking was cancelled.</span>
      </div>
    );
  }

  // Find the index of the current status
  const currentIndex = STATUS_STAGES.findIndex(s => s.id === currentStatus);
  const activeOrder = currentIndex !== -1 ? STATUS_STAGES[currentIndex].order : 1;

  return (
    <div className="flex items-center justify-between w-full max-w-3xl mx-auto px-4 mt-6 mb-2">
      {STATUS_STAGES.map((step, idx) => {
        const isCompleted = step.order < activeOrder;
        const isCurrent = step.order === activeOrder;
        const isLast = idx === STATUS_STAGES.length - 1;

        return (
          <React.Fragment key={step.id}>
            {/* Step Node */}
            <div className="relative flex flex-col items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center z-10 
                  ${isCompleted ? 'bg-primary text-white' : 
                    isCurrent ? 'bg-info text-white ring-4 ring-info/20 animate-pulse-soft' : 
                  'bg-white border-2 border-neutral-300 text-neutral-400'}`}
              >
                {isCompleted ? (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                ) : isCurrent ? (
                  <span className="w-2.5 h-2.5 bg-white rounded-full"></span>
                ) : (
                  <span className="text-[11px] font-bold">{step.order}</span>
                )}
              </div>
              <span 
                className={`absolute top-10 text-[11px] font-bold whitespace-nowrap hidden sm:block
                  ${isCompleted || isCurrent ? 'text-neutral-900' : 'text-neutral-400'}`}
              >
                {step.label}
              </span>
            </div>

            {/* Connector Line */}
            {!isLast && (
              <div className="flex-1 mx-2 relative top-[-10px] sm:top-[-10px]">
                <div 
                  className={`h-[3px] rounded-full w-full transition-colors duration-300
                    ${step.order < activeOrder ? 'bg-primary' : 'bg-neutral-200'}`} 
                />
              </div>
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
}
