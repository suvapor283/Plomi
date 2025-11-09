import Link from 'next/link';
import {
  Bars3Icon,
  ChatBubbleOvalLeftIcon,
  BellIcon,
  AdjustmentsHorizontalIcon,
} from '@heroicons/react/24/outline';

import { Button } from '@/components/ui/button';

export default function Header() {
  return (
    <nav className="relative z-10 flex justify-between px-6 py-3 shadow-md">
      <div className="flex items-center sm:space-x-4">
        <Bars3Icon className="size-7" />
        <Link href="/" className="cursor-pointer font-semibold sm:text-xl">
          Plomi
        </Link>
      </div>
      <div className="flex items-center sm:space-x-4">
        {[BellIcon, ChatBubbleOvalLeftIcon, AdjustmentsHorizontalIcon].map(
          (Icon, idx) => (
            <Button key={idx} variant="ghost" size="icon">
              <Icon className="size-4 sm:size-6" />
            </Button>
          )
        )}
      </div>
    </nav>
  );
}
